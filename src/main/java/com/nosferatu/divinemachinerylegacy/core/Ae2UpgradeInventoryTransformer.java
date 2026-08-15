package com.nosferatu.divinemachinerylegacy.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Backports bmaddon's UpgradeInventoryMixin to AE2 rv3.
 *
 * The patch is intentionally surgical: only integer returns from
 * UpgradeInventory#getInstalledUpgrades(Upgrades) are passed through the DML
 * helper. Every non-SPEED upgrade is returned unchanged.
 */
public final class Ae2UpgradeInventoryTransformer implements IClassTransformer {
    private static final String TARGET = "appeng.parts.automation.UpgradeInventory";
    private static final String METHOD = "getInstalledUpgrades";
    private static final String DESC = "(Lappeng/api/config/Upgrades;)I";
    private static final String HELPER =
            "com/nosferatu/divinemachinerylegacy/bloodmagic/BloodMagicAe2SpeedIntegration";
    private static final String HELPER_DESC =
            "(Lappeng/parts/automation/UpgradeInventory;Lappeng/api/config/Upgrades;I)I";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || (!TARGET.equals(name) && !TARGET.equals(transformedName))) {
            return basicClass;
        }

        try {
            ClassNode node = new ClassNode();
            new ClassReader(basicClass).accept(node, 0);
            boolean changed = false;

            for (MethodNode method : node.methods) {
                if (!METHOD.equals(method.name) || !DESC.equals(method.desc)) continue;

                final int resultLocal = method.maxLocals;
                method.maxLocals += 1;

                for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; ) {
                    AbstractInsnNode next = insn.getNext();
                    if (insn.getOpcode() == Opcodes.IRETURN) {
                        InsnList hook = new InsnList();
                        hook.add(new VarInsnNode(Opcodes.ISTORE, resultLocal));
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        hook.add(new VarInsnNode(Opcodes.ILOAD, resultLocal));
                        hook.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                HELPER,
                                "adjustInstalledSpeed",
                                HELPER_DESC,
                                false));
                        method.instructions.insertBefore(insn, hook);
                        changed = true;
                    }
                    insn = next;
                }
                break;
            }

            if (!changed) {
                System.err.println("[DivineMachineryLegacy] AE2 UpgradeInventory hook was not applied; "
                        + "unexpected AE2 build detected.");
                return basicClass;
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[DivineMachineryLegacy] Failed to patch AE2 UpgradeInventory: " + error);
            error.printStackTrace();
            return basicClass;
        }
    }
}
