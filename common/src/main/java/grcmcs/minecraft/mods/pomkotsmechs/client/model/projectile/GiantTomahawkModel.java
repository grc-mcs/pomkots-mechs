package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.GiantTomahawkEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class GiantTomahawkModel extends EntityModel<GiantTomahawkEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "giant_tomahawk"), "main");
    private final ModelPart root;
	private final ModelPart heat_saber;
	private final ModelPart bone2;
	private final ModelPart bone3;

    public GiantTomahawkModel(ModelPart root) {
        this.root = root.getChild("root");
		this.heat_saber = this.root.getChild("heat_saber");
		this.bone2 = this.heat_saber.getChild("bone2");
		this.bone3 = this.heat_saber.getChild("bone3");
    }

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 3.1416F, 0.0F, 0.0F));

		PartDefinition heat_saber = root.addOrReplaceChild("heat_saber", CubeListBuilder.create().texOffs(414, 171).mirror().addBox(-5.3421F, 1.1634F, 6.7725F, 10.5845F, 11.1131F, 3.5079F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(315, 302).addBox(-3.9412F, 3.2743F, -18.6776F, 7.1845F, 6.1631F, 22.0579F, new CubeDeformation(0.0F))
		.texOffs(427, 170).mirror().addBox(-4.2682F, 2.9353F, 8.7749F, 8.0605F, 7.0105F, 11.963F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(289, 309).mirror().addBox(-4.2837F, 2.2198F, -59.1671F, 9.0915F, 8.0915F, 28.8985F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(395, 159).mirror().addBox(-2.2837F, 4.2198F, -73.1671F, 4.0915F, 4.0915F, 51.8985F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(402, 163).mirror().addBox(-2.2837F, 4.2198F, -21.1671F, 4.0915F, 4.0915F, 45.8985F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(428, 172).mirror().addBox(-3.3721F, 2.1511F, -70.2171F, 5.9683F, 8.0915F, 7.9365F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.1799F, -26.0112F, -6.832F, 1.5708F, 0.0F, 0.0F));

		PartDefinition bone2 = heat_saber.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(440, 182).mirror().addBox(-1.1522F, -2.6666F, -2.467F, 2.3044F, 15.9334F, 5.1962F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(427, 170).mirror().addBox(-1.8122F, -5.3066F, -7.087F, 3.6244F, 3.3934F, 13.1162F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(8, 472).addBox(-2.4264F, -7.1535F, -7.5295F, 4.8529F, 3.2512F, 13.2287F, new CubeDeformation(0.0F)), PartPose.offset(-0.238F, -8.5903F, -54.8151F));

		PartDefinition heat_hawk_r1 = bone2.addOrReplaceChild("heat_hawk_r1", CubeListBuilder.create().texOffs(426, 172).mirror().addBox(-4.1322F, -4.3367F, -8.8681F, 8.2644F, 6.6934F, 11.7962F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0101F, -14.0589F, -2.3126F, 0.0F, 0.0F));

		PartDefinition heat_hawk_r2 = bone2.addOrReplaceChild("heat_hawk_r2", CubeListBuilder.create().texOffs(426, 170).mirror().addBox(-2.8122F, -1.3502F, -8.1012F, 5.6244F, 2.7004F, 8.8727F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -5.7863F, -7.4059F, 0.7854F, 0.0F, 0.0F));

		PartDefinition heat_hawk_r3 = bone2.addOrReplaceChild("heat_hawk_r3", CubeListBuilder.create().texOffs(421, 165).mirror().addBox(-2.8122F, -1.3502F, -0.7715F, 5.6244F, 4.7004F, 12.1727F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -5.7863F, 5.62F, -0.48F, 0.0F, 0.0F));

		PartDefinition heat_hawk_r4 = bone2.addOrReplaceChild("heat_hawk_r4", CubeListBuilder.create().texOffs(417, 163).mirror().addBox(-4.1322F, -1.6967F, -8.8681F, 8.2644F, 4.7134F, 17.7362F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.0101F, 11.6811F, -0.48F, 0.0F, 0.0F));

		PartDefinition heat_hawk_r5 = bone2.addOrReplaceChild("heat_hawk_r5", CubeListBuilder.create().texOffs(425, 168).mirror().addBox(-1.8122F, -2.3667F, -6.2281F, 3.6244F, 3.7334F, 15.4562F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 4.6401F, -6.4789F, -0.6981F, 0.0F, 0.0F));

		PartDefinition heat_hawk_r6 = bone2.addOrReplaceChild("heat_hawk_r6", CubeListBuilder.create().texOffs(421, 164).mirror().addBox(-1.8122F, -2.3667F, -13.2281F, 3.6244F, 4.7334F, 19.4562F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 4.6401F, 11.4011F, 0.6109F, 0.0F, 0.0F));

		PartDefinition bone3 = heat_saber.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(440, 182).mirror().addBox(-1.1522F, -13.2668F, -2.467F, 2.3044F, 15.9334F, 5.1962F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(427, 170).mirror().addBox(-1.8122F, 1.9132F, -7.087F, 3.6244F, 3.3934F, 13.1162F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(10, 472).addBox(-2.4264F, 3.9023F, -7.5295F, 4.8529F, 3.2512F, 13.2287F, new CubeDeformation(0.0F)), PartPose.offset(-0.238F, 20.6128F, -54.8151F));

		PartDefinition heat_hawk_r7 = bone3.addOrReplaceChild("heat_hawk_r7", CubeListBuilder.create().texOffs(426, 172).mirror().addBox(-4.1322F, -2.3567F, -8.8681F, 8.2644F, 6.6934F, 11.7962F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.0101F, -14.0589F, 2.3126F, 0.0F, 0.0F));

		PartDefinition heat_hawk_r8 = bone3.addOrReplaceChild("heat_hawk_r8", CubeListBuilder.create().texOffs(426, 170).mirror().addBox(-2.8122F, -1.3502F, -8.1012F, 5.6244F, 2.7004F, 8.8727F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 5.7863F, -7.4059F, -0.7854F, 0.0F, 0.0F));

		PartDefinition heat_hawk_r9 = bone3.addOrReplaceChild("heat_hawk_r9", CubeListBuilder.create().texOffs(421, 165).mirror().addBox(-2.8122F, -3.3502F, -0.7715F, 5.6244F, 4.7004F, 12.1727F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 5.7863F, 5.62F, 0.48F, 0.0F, 0.0F));

		PartDefinition heat_hawk_r10 = bone3.addOrReplaceChild("heat_hawk_r10", CubeListBuilder.create().texOffs(417, 163).mirror().addBox(-4.1322F, -3.0167F, -8.8681F, 8.2644F, 4.7134F, 17.7362F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.0101F, 11.6811F, 0.48F, 0.0F, 0.0F));

		PartDefinition heat_hawk_r11 = bone3.addOrReplaceChild("heat_hawk_r11", CubeListBuilder.create().texOffs(425, 168).mirror().addBox(-1.8122F, -1.3667F, -6.2281F, 3.6244F, 3.7334F, 15.4562F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -4.6401F, -6.4789F, 0.6981F, 0.0F, 0.0F));

		PartDefinition heat_hawk_r12 = bone3.addOrReplaceChild("heat_hawk_r12", CubeListBuilder.create().texOffs(421, 164).mirror().addBox(-1.8122F, -2.3667F, -13.2281F, 3.6244F, 4.7334F, 19.4562F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -4.6401F, 11.4011F, -0.6109F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}

    @Override
    public void setupAnim(GiantTomahawkEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
