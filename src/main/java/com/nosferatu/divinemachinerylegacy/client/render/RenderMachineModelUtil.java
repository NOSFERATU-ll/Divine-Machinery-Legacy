package com.nosferatu.divinemachinerylegacy.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;

/** Small 1.7.10 cuboid/plane renderer used by the Reforked machine models. */
public final class RenderMachineModelUtil {
    public static final double U = 1D / 16D;
    public static final UV FULL = new UV(0, 0, 16, 16);
    public static final UV FRAME_EDGE = new UV(0, 0, 16, 1);
    public static final UV FRAME_POST = new UV(0, 1, 1, 15);
    public static final UV FRAME_CAP = new UV(0, 0, 1, 1);
    public static final UV FRAME_RAIL = new UV(1, 0, 15, 1);

    private RenderMachineModelUtil() {}

    public static void frame(Tessellator t, IIcon icon, double ox, double oy, double oz, int brightness) {
        box(t, ox, oy, oz, 0,0,0,16,1,16, brightness,
                icon,FULL, icon,FULL, icon,FRAME_EDGE, icon,FRAME_EDGE, icon,FRAME_EDGE, icon,FRAME_EDGE);
        post(t, icon, ox,oy,oz, 0,1,0,1,15,1, brightness);
        post(t, icon, ox,oy,oz, 15,1,0,16,15,1, brightness);
        post(t, icon, ox,oy,oz, 0,1,15,1,15,16, brightness);
        post(t, icon, ox,oy,oz, 15,1,15,16,15,16, brightness);
        box(t,ox,oy,oz,0,15,1,1,16,15,brightness,
                icon,FRAME_POST,icon,FRAME_POST,icon,FRAME_CAP,icon,FRAME_CAP,icon,FRAME_RAIL,icon,FRAME_RAIL);
        box(t,ox,oy,oz,15,15,1,16,16,15,brightness,
                icon,FRAME_POST,icon,FRAME_POST,icon,FRAME_CAP,icon,FRAME_CAP,icon,FRAME_RAIL,icon,FRAME_RAIL);
        box(t,ox,oy,oz,0,15,0,16,16,1,brightness,
                icon,FRAME_EDGE,icon,FRAME_EDGE,icon,FRAME_EDGE,icon,FRAME_EDGE,icon,FRAME_CAP,icon,FRAME_CAP);
        box(t,ox,oy,oz,0,15,15,16,16,16,brightness,
                icon,FRAME_EDGE,icon,FRAME_EDGE,icon,FRAME_EDGE,icon,FRAME_EDGE,icon,FRAME_CAP,icon,FRAME_CAP);
    }

    private static void post(Tessellator t, IIcon icon, double ox,double oy,double oz,
                             double x1,double y1,double z1,double x2,double y2,double z2,int brightness) {
        box(t,ox,oy,oz,x1,y1,z1,x2,y2,z2,brightness,
                icon,FRAME_CAP,icon,FRAME_CAP,icon,FRAME_POST,icon,FRAME_POST,icon,FRAME_POST,icon,FRAME_POST);
    }

    public static void box(Tessellator t, double ox,double oy,double oz,
                           double px1,double py1,double pz1,double px2,double py2,double pz2,
                           int brightness,
                           IIcon down,UV duv,IIcon up,UV uuv,IIcon north,UV nuv,IIcon south,UV suv,
                           IIcon west,UV wuv,IIcon east,UV euv) {
        double x1=ox+px1*U,y1=oy+py1*U,z1=oz+pz1*U;
        double x2=ox+px2*U,y2=oy+py2*U,z2=oz+pz2*U;
        if(down!=null) down(t,x1,y1,z1,x2,z2,down,duv,brightness);
        if(up!=null) up(t,x1,y2,z1,x2,z2,up,uuv,brightness);
        if(north!=null) north(t,x1,y1,z1,x2,y2,north,nuv,brightness);
        if(south!=null) south(t,x1,y1,z2,x2,y2,south,suv,brightness);
        if(west!=null) west(t,x1,y1,z1,y2,z2,west,wuv,brightness);
        if(east!=null) east(t,x2,y1,z1,y2,z2,east,euv,brightness);
    }

    public static void crossedPlant(Tessellator t,double ox,double oy,double oz,
                                    double x1,double y1,double z1,double x2,double y2,double z2,
                                    IIcon icon,int brightness) {
        prep(t,brightness,1F,0,1,0);
        double u0=icon.getMinU(),u1=icon.getMaxU(),v0=icon.getMinV(),v1=icon.getMaxV();
        double ax=ox+x1*U, ay=oy+y1*U, az=oz+z1*U;
        double bx=ox+x2*U, by=oy+y2*U, bz=oz+z2*U;
        t.addVertexWithUV(ax,by,az,u0,v0); t.addVertexWithUV(ax,ay,az,u0,v1); t.addVertexWithUV(bx,ay,bz,u1,v1); t.addVertexWithUV(bx,by,bz,u1,v0);
        t.addVertexWithUV(bx,by,bz,u0,v0); t.addVertexWithUV(bx,ay,bz,u0,v1); t.addVertexWithUV(ax,ay,az,u1,v1); t.addVertexWithUV(ax,by,az,u1,v0);
        double cx=ox+x2*U, cz=oz+z1*U, dx=ox+x1*U, dz=oz+z2*U;
        t.addVertexWithUV(cx,by,cz,u0,v0); t.addVertexWithUV(cx,ay,cz,u0,v1); t.addVertexWithUV(dx,ay,dz,u1,v1); t.addVertexWithUV(dx,by,dz,u1,v0);
        t.addVertexWithUV(dx,by,dz,u0,v0); t.addVertexWithUV(dx,ay,dz,u0,v1); t.addVertexWithUV(cx,ay,cz,u1,v1); t.addVertexWithUV(cx,by,cz,u1,v0);
    }

    private static void down(Tessellator t,double x1,double y,double z1,double x2,double z2,IIcon i,UV uv,int b){prep(t,b,.5F,0,-1,0);double u0=i.getInterpolatedU(uv.u0),u1=i.getInterpolatedU(uv.u1),v0=i.getInterpolatedV(uv.v0),v1=i.getInterpolatedV(uv.v1);t.addVertexWithUV(x1,y,z2,u0,v1);t.addVertexWithUV(x1,y,z1,u0,v0);t.addVertexWithUV(x2,y,z1,u1,v0);t.addVertexWithUV(x2,y,z2,u1,v1);}
    private static void up(Tessellator t,double x1,double y,double z1,double x2,double z2,IIcon i,UV uv,int b){prep(t,b,1F,0,1,0);double u0=i.getInterpolatedU(uv.u0),u1=i.getInterpolatedU(uv.u1),v0=i.getInterpolatedV(uv.v0),v1=i.getInterpolatedV(uv.v1);t.addVertexWithUV(x1,y,z1,u0,v0);t.addVertexWithUV(x1,y,z2,u0,v1);t.addVertexWithUV(x2,y,z2,u1,v1);t.addVertexWithUV(x2,y,z1,u1,v0);}
    private static void north(Tessellator t,double x1,double y1,double z,double x2,double y2,IIcon i,UV uv,int b){prep(t,b,.8F,0,0,-1);double u0=i.getInterpolatedU(uv.u0),u1=i.getInterpolatedU(uv.u1),v0=i.getInterpolatedV(uv.v0),v1=i.getInterpolatedV(uv.v1);t.addVertexWithUV(x2,y2,z,u0,v0);t.addVertexWithUV(x2,y1,z,u0,v1);t.addVertexWithUV(x1,y1,z,u1,v1);t.addVertexWithUV(x1,y2,z,u1,v0);}
    private static void south(Tessellator t,double x1,double y1,double z,double x2,double y2,IIcon i,UV uv,int b){prep(t,b,.8F,0,0,1);double u0=i.getInterpolatedU(uv.u0),u1=i.getInterpolatedU(uv.u1),v0=i.getInterpolatedV(uv.v0),v1=i.getInterpolatedV(uv.v1);t.addVertexWithUV(x1,y2,z,u0,v0);t.addVertexWithUV(x1,y1,z,u0,v1);t.addVertexWithUV(x2,y1,z,u1,v1);t.addVertexWithUV(x2,y2,z,u1,v0);}
    private static void west(Tessellator t,double x,double y1,double z1,double y2,double z2,IIcon i,UV uv,int b){prep(t,b,.6F,-1,0,0);double u0=i.getInterpolatedU(uv.u0),u1=i.getInterpolatedU(uv.u1),v0=i.getInterpolatedV(uv.v0),v1=i.getInterpolatedV(uv.v1);t.addVertexWithUV(x,y2,z1,u0,v0);t.addVertexWithUV(x,y1,z1,u0,v1);t.addVertexWithUV(x,y1,z2,u1,v1);t.addVertexWithUV(x,y2,z2,u1,v0);}
    private static void east(Tessellator t,double x,double y1,double z1,double y2,double z2,IIcon i,UV uv,int b){prep(t,b,.6F,1,0,0);double u0=i.getInterpolatedU(uv.u0),u1=i.getInterpolatedU(uv.u1),v0=i.getInterpolatedV(uv.v0),v1=i.getInterpolatedV(uv.v1);t.addVertexWithUV(x,y2,z2,u0,v0);t.addVertexWithUV(x,y1,z2,u0,v1);t.addVertexWithUV(x,y1,z1,u1,v1);t.addVertexWithUV(x,y2,z1,u1,v0);}
    private static void prep(Tessellator t,int b,float s,float nx,float ny,float nz){t.setBrightness(b);t.setColorOpaque_F(s,s,s);t.setNormal(nx,ny,nz);}

    public static final class UV {
        public final double u0,v0,u1,v1;
        public UV(double u0,double v0,double u1,double v1){this.u0=u0;this.v0=v0;this.u1=u1;this.v1=v1;}
    }
}
