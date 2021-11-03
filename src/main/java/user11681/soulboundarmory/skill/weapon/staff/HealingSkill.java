package user11681.soulboundarmory.skill.weapon.staff;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import user11681.cell.client.gui.screen.CellScreen;
import user11681.soulboundarmory.skill.Skill;

public class HealingSkill extends Skill {
    public HealingSkill(Identifier identifier) {
        super(identifier);
    }

    @Override
    public int cost(boolean learned, int level) {
        return 2;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(CellScreen screen, MatrixStack matrices, int level, int x, int y, int zOffset) {
        screen.renderGuiItem(PotionUtil.setPotion(Items.POTION.getDefaultStack(), Potions.HEALING), x, y, zOffset);
    }
}
