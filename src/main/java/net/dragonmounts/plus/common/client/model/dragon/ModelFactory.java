package net.dragonmounts.plus.common.client.model.dragon;

import net.dragonmounts.plus.common.client.ClientUtil;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import static net.dragonmounts.plus.common.client.ClientUtil.scaledPose;
import static net.dragonmounts.plus.common.client.model.dragon.BuiltinFactory.*;
import static net.dragonmounts.plus.common.client.model.dragon.DragonModel.*;
import static net.dragonmounts.plus.common.entity.dragon.DragonModelContracts.*;
import static net.dragonmounts.plus.common.util.math.MathUtil.TO_RAD_FACTOR;

public interface ModelFactory {
    default LayerDefinition makeModel() {
        var model = new MeshDefinition();
        var root = model.getRoot();
        var body = this.makeBody(root);
        this.makeChest(body);
        this.makeSaddle(body);
        this.makeHead(root);
        this.makeNeck(root);
        this.makeFrontLegs(root);
        this.makeHindLegs(root);
        this.makeLeftWing(root);
        this.makeRightWing(root);
        this.makeTail(root);
        return LayerDefinition.create(model, 256, 256);
    }

    default PartDefinition makeBody(PartDefinition root) {
        var body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        //body
                        .texOffs(0, 0)
                        .addBox(-12, 0, -16, 24, 24, 64)
                        //scale
                        .texOffs(0, 32)
                        .addBox(-1, -6, 10, 2, 6, 12)
                        .addBox(-1, -6, 30, 2, 6, 12),
                PartPose.offset(0, 4, 8)
        );
        body.addOrReplaceChild(
                "back",
                CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(-1, -6, -10, 2, 6, 12),
                PartPose.ZERO
        );
        return body;
    }

    default void makeNeck(PartDefinition root) {
        var builder = CubeListBuilder.create();
        var neck = root.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.ZERO);
        var normal = builder.texOffs(112, 88).addBox(-5, -5, -5, NECK_SIZE, NECK_SIZE, NECK_SIZE).getCubes();
        var scaled = builder.texOffs(0, 0).addBox(-1, -7, -3, 2, 4, 6).getCubes();
        for (int i = 0; i < NECK_SEGMENTS; ++i) {
            float scale = calcNeckSize(i);
            neck.addOrReplaceChild(
                    ClientUtil.toString(i),
                    new PartDefinition((i & 1) == 1 || i == 0 ? normal : scaled, scaledPose(scale, scale, 0.6F))
            );
        }
    }

    default void makeHead(PartDefinition root) {
        var head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox("head", -8.0F, -8.0F, 6.0F + HEAD_OFS, HEAD_SIZE, HEAD_SIZE, HEAD_SIZE)
                        .texOffs(56, 88)
                        .addBox("upper_jaw", -6.0F, -1.0F, -8.0F + HEAD_OFS, JAW_WIDTH, JAW_HEIGHT, JAW_LENGTH)
                        .texOffs(48, 0)
                        .addBox("nostril", -5.0F, -3.0F, -6.0F + HEAD_OFS, 2.0F, 2.0F, 4.0F).mirror()
                        .addBox("nostril", 3.0F, -3.0F, -6.0F + HEAD_OFS, 2.0F, 2.0F, 4.0F),
                PartPose.ZERO
        );
        float rad30 = 30 * TO_RAD_FACTOR;
        head.addOrReplaceChild(
                "left_horn",
                CubeListBuilder.create()
                        .addBox("horn", HORN_OFS, HORN_OFS, HORN_OFS, HORN_THICK, HORN_THICK, HORN_LENGTH, 28, 32),
                PartPose.offsetAndRotation(-5, -8, 0, rad30, -rad30, 0)
        );
        head.addOrReplaceChild(
                "right_horn",
                CubeListBuilder.create()
                        .addBox("horn", HORN_OFS, HORN_OFS, HORN_OFS, HORN_THICK, HORN_THICK, HORN_LENGTH, 28, 32)
                        .mirror(),
                PartPose.offsetAndRotation(5, -8, 0, rad30, rad30, 0)
        );
        head.addOrReplaceChild(
                "jaw",
                CubeListBuilder.create()
                        .texOffs(0, 88)
                        .addBox("lower_jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16),
                PartPose.offset(0.0F, 4.0F, 8.0F + HEAD_OFS)
        );
    }

    default void makeFrontLegs(PartDefinition root) {
        makeFrontLeg(root, "left_front_leg", NORMAL_LEG_WIDTH, LEG_LENGTH, true, PartPose.offset(-11, 18, 4));
        makeFrontLeg(root, "right_front_leg", NORMAL_LEG_WIDTH, LEG_LENGTH, false, PartPose.offset(11, 18, 4));
    }

    default void makeHindLegs(PartDefinition root) {
        makeHindLeg(root, "left_hind_leg", NORMAL_LEG_WIDTH, LEG_LENGTH, true, PartPose.offset(-11, 13, 46));
        makeHindLeg(root, "right_hind_leg", NORMAL_LEG_WIDTH, LEG_LENGTH, false, PartPose.offset(11, 13, 46));
    }

    default void makeLeftWing(PartDefinition root) {
        var common = CubeListBuilder.create()
                .mirror(true)
                .texOffs(0, 172)
                .addBox(0, -1, -1, 70, 2, 2)
                .texOffs(-49, 176)
                .addBox(0, 0, 1, 70, 0, 48);
        var fingers = root.addOrReplaceChild(
                "left_wing",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 152)
                        .addBox(0, -3, -3, 28, 6, 6)
                        .texOffs(116, 232)
                        .addBox(0, 0, 2, 28, 0, 24),
                new PartPose(10, 5, 4, 0.0F, -1.4F, -0.8F, 1.1F, 1.1F, 1.1F)
        ).addOrReplaceChild(
                "forearm",
                CubeListBuilder.create()
                        .texOffs(0, 164)
                        .addBox(0, -2, -2, 48, 4, 4),
                PartPose.offsetAndRotation(28, 0, 0, 0.0F, 2.8F, 0.0F)
        ).addOrReplaceChild("fingers", CubeListBuilder.create(), PartPose.ZERO);
        fingers.addOrReplaceChild("0", common, PartPose.offsetAndRotation(47, 0, 0, 0.000F, -2.7F, 0.0F));
        fingers.addOrReplaceChild("1", common, PartPose.offsetAndRotation(47, 0, 0, 0.005F, -2.8F, 0.0F));
        fingers.addOrReplaceChild("2", common, PartPose.offsetAndRotation(47, 0, 0, 0.010F, -2.9F, 0.0F));
        fingers.addOrReplaceChild(
                "3",
                CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 172)
                        .addBox(0, -1, -1, 70, 2, 2)
                        .texOffs(-32, 224)
                        .addBox(0, 0, 1, 70, 0, 32),
                PartPose.offsetAndRotation(47, 0, 0, 0.015F, -3.0F, 0.0F)
        );
    }

    default void makeRightWing(PartDefinition root) {
        var common = CubeListBuilder.create()
                .mirror(false)
                .texOffs(0, 172)
                .addBox(-70, -1, -1, 70, 2, 2)
                .texOffs(-49, 176)
                .addBox(-70, 0, 1, 70, 0, 48);
        var fingers = root.addOrReplaceChild(
                "right_wing",
                CubeListBuilder.create()
                        .mirror(false)
                        .texOffs(0, 152)
                        .addBox(-28, -3, -3, 28, 6, 6)
                        .texOffs(116, 232)
                        .addBox(-28, 0, 2, 28, 0, 24),
                new PartPose(-10, 5, 4, 0.0F, 1.4F, 0.8F, 1.1F, 1.1F, 1.1F)
        ).addOrReplaceChild(
                "forearm",
                CubeListBuilder.create()
                        .texOffs(0, 164)
                        .addBox(-48, -2, -2, 48, 4, 4),
                PartPose.offsetAndRotation(-28, 0, 0, 0.0F, -2.8F, 0.0F)
        ).addOrReplaceChild("fingers", CubeListBuilder.create(), PartPose.ZERO);
        fingers.addOrReplaceChild("0", common, PartPose.offsetAndRotation(-47, 0, 0, 0.000F, 2.7F, 0.0F));
        fingers.addOrReplaceChild("1", common, PartPose.offsetAndRotation(-47, 0, 0, 0.005F, 2.8F, 0.0F));
        fingers.addOrReplaceChild("2", common, PartPose.offsetAndRotation(-47, 0, 0, 0.010F, 2.9F, 0.0F));
        fingers.addOrReplaceChild(
                "3",
                CubeListBuilder.create()
                        .mirror(false)
                        .texOffs(0, 172)
                        .addBox(-70, -1, -1, 70, 2, 2)
                        .texOffs(-32, 224)
                        .addBox(-70, 0, 1, 70, 0, 32),
                PartPose.offsetAndRotation(-47, 0, 0, 0.015F, 3.0F, 0.0F)
        );
    }

    default void makeTail(PartDefinition root) {
        var tail = root.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 62.0F));
        var segment = CubeListBuilder.create()
                .texOffs(152, 88)
                .addBox(-5, -5, -5, TAIL_SIZE, TAIL_SIZE, TAIL_SIZE)
                .texOffs(0, 0)
                .addBox(-1, -8, -3, 2, 4, 6)
                .getCubes();
        for (int i = 0; i < TAIL_SEGMENTS; ++i) {
            tail.addOrReplaceChild(ClientUtil.toString(i), new PartDefinition(segment, scaledPose(calcTailSize(i))));
        }
    }

    default void makeChest(PartDefinition body) {
        body.addOrReplaceChild(
                "chest",
                CubeListBuilder.create()
                        .texOffs(192, 132)
                        .addBox(12, 0, 21, 4, 12, 12)
                        .texOffs(224, 132)
                        .addBox(-16, 0, 21, 4, 12, 12),
                PartPose.ZERO
        );
    }

    default void makeSaddle(PartDefinition body) {
        body.addOrReplaceChild(
                "saddle",
                CubeListBuilder.create()
                        .texOffs(184, 98)
                        .addBox(-7, -2, -15, 15, 3, 20)
                        .texOffs(214, 120)
                        .addBox(-3, -3, -14, 6, 1, 2)
                        .addBox(-6, -4, 2, 13, 2, 2)
                        .texOffs(220, 100)
                        .addBox(12, 0, -14, 1, 14, 2)
                        .addBox(-13, 0, -14, 1, 10, 2)
                        .texOffs(224, 132)
                        .addBox(12, 14, -15, 1, 5, 4)
                        .addBox(-13, 10, -15, 1, 5, 4),
                PartPose.ZERO
        );
    }
}
