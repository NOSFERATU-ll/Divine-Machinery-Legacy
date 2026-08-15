package com.nosferatu.divinemachinerylegacy.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Small AE2 rv3 compatibility transformer for the behaviors that bmaddon
 * implements with modern Mixins: global weighted speed cards and direct Blood
 * Pattern support in the Pattern Encoding Terminal.
 */
public final class Ae2UpgradeInventoryTransformer implements IClassTransformer {
    private static final String UPGRADE_TARGET = "appeng.parts.automation.UpgradeInventory";
    private static final String SLOT_TARGET = "appeng.container.slot.SlotRestrictedInput";
    private static final String PATTERN_TERM_TARGET = "appeng.container.implementations.ContainerPatternTerm";

    private static final String SPEED_HELPER =
            "com/nosferatu/divinemachinerylegacy/bloodmagic/BloodMagicAe2SpeedIntegration";
    private static final String PATTERN_HELPER =
            "com/nosferatu/divinemachinerylegacy/bloodmagic/BloodMagicAe2PatternTerminalIntegration";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        String className = transformedName == null ? name : transformedName;

        try {
            if (UPGRADE_TARGET.equals(className) || UPGRADE_TARGET.equals(name)) {
                return patchUpgradeInventory(basicClass);
            }
            if (SLOT_TARGET.equals(className) || SLOT_TARGET.equals(name)) {
                return patchRestrictedInputSlot(basicClass);
            }
            if (PATTERN_TERM_TARGET.equals(className) || PATTERN_TERM_TARGET.equals(name)) {
                return patchPatternTerminal(basicClass);
            }
        } catch (Throwable error) {
            System.err.println("[DivineMachineryLegacy] Failed to patch " + className + ": " + error);
            error.printStackTrace();
        }
        return basicClass;
    }

    private byte[] patchUpgradeInventory(byte[] basicClass) {
        ClassNode node = read(basicClass);
        boolean changed = false;

        for (MethodNode method : node.methods) {
            if (!"getInstalledUpgrades".equals(method.name)
                    || !"(Lappeng/api/config/Upgrades;)I".equals(method.desc)) continue;

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
                            SPEED_HELPER,
                            "adjustInstalledSpeed",
                            "(Lappeng/parts/automation/UpgradeInventory;Lappeng/api/config/Upgrades;I)I",
                            false));
                    method.instructions.insertBefore(insn, hook);
                    changed = true;
                }
                insn = next;
            }
            break;
        }

        return finish(node, changed, "UpgradeInventory#getInstalledUpgrades", basicClass);
    }

    private byte[] patchRestrictedInputSlot(byte[] basicClass) {
        ClassNode node = read(basicClass);
        boolean changed = false;

        for (MethodNode method : node.methods) {
            if (!"isItemValid".equals(method.name)
                    || !"(Lnet/minecraft/item/ItemStack;)Z".equals(method.desc)) continue;

            boolean sawBlankPatternDefinition = false;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) insn;

                if ("appeng/api/definitions/IMaterials".equals(call.owner)
                        && "blankPattern".equals(call.name)) {
                    sawBlankPatternDefinition = true;
                    continue;
                }

                if (sawBlankPatternDefinition
                        && "appeng/api/definitions/IItemDefinition".equals(call.owner)
                        && "isSameAs".equals(call.name)
                        && "(Lnet/minecraft/item/ItemStack;)Z".equals(call.desc)) {
                    // The stack currently holds AE2's normal boolean result.
                    // OR it with "blank Blood Pattern" without bypassing any of
                    // SlotRestrictedInput's earlier container/edit checks.
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    hook.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            PATTERN_HELPER,
                            "acceptAsBlankPattern",
                            "(ZLnet/minecraft/item/ItemStack;)Z",
                            false));
                    method.instructions.insert(call, hook);
                    changed = true;
                    break;
                }
            }
            break;
        }

        return finish(node, changed, "SlotRestrictedInput BLANK_PATTERN", basicClass);
    }

    private byte[] patchPatternTerminal(byte[] basicClass) {
        ClassNode node = read(basicClass);
        boolean changed = false;

        for (MethodNode method : node.methods) {
            if (!"encode".equals(method.name) || !"()V".equals(method.desc)) continue;

            LabelNode vanilla = new LabelNode();
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
            hook.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    PATTERN_HELPER,
                    "tryEncode",
                    "(Lappeng/container/implementations/ContainerPatternTerm;)Z",
                    false));
            hook.add(new JumpInsnNode(Opcodes.IFEQ, vanilla));
            hook.add(new org.objectweb.asm.tree.InsnNode(Opcodes.RETURN));
            hook.add(vanilla);
            method.instructions.insert(hook);
            changed = true;
            break;
        }

        return finish(node, changed, "ContainerPatternTerm#encode", basicClass);
    }

    private ClassNode read(byte[] basicClass) {
        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);
        return node;
    }

    private byte[] finish(ClassNode node, boolean changed, String hookName, byte[] original) {
        if (!changed) {
            System.err.println("[DivineMachineryLegacy] AE2 hook was not applied: " + hookName
                    + ". Unexpected AE2 build detected.");
            return original;
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
