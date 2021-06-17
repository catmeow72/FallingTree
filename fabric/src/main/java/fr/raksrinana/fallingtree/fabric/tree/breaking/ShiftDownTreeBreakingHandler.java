package fr.raksrinana.fallingtree.fabric.tree.breaking;

import fr.raksrinana.fallingtree.fabric.tree.Tree;
import fr.raksrinana.fallingtree.fabric.tree.TreePart;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.Collection;
import java.util.List;
import static fr.raksrinana.fallingtree.fabric.FallingTree.config;
import static fr.raksrinana.fallingtree.fabric.utils.TreePartType.NETHER_WART;
import static java.util.Objects.isNull;
import static net.minecraft.Util.NIL_UUID;

public class ShiftDownTreeBreakingHandler implements ITreeBreakingHandler{
	private static ShiftDownTreeBreakingHandler INSTANCE;
	
	@Override
	public boolean breakTree(Player player, Tree tree){
		return destroyShift(tree, player, player.getMainHandItem());
	}
	
	private boolean destroyShift(Tree tree, Player player, ItemStack tool){
		
		tree.getLastSequencePart()
				.map(treePart -> {
					var level = tree.getLevel();
					if(treePart.treePartType() == NETHER_WART && config.getTrees().isInstantlyBreakWarts()){
						return breakElements(level, player, tool, tree, tree.getWarts());
					}
					else{
						return breakElements(level, player, tool, tree, List.of(treePart));
					}
				});
		
		return false;
	}
	
	private boolean breakElements(Level level, Player player, ItemStack tool, Tree tree, Collection<TreePart> parts){
		var count = parts.size();
		var damageMultiplicand = config.getTools().getDamageMultiplicand();
		
		if(checkTools(player, tool, damageMultiplicand, count)){
			var breakCount = parts.stream()
					.mapToInt(wart -> breakPart(tree, wart, level, player, tool, damageMultiplicand))
					.sum();
			
			tool.hurtAndBreak(Math.max(1, damageMultiplicand * breakCount), player, (entity) -> {});
			return true;
		}
		
		return false;
	}
	
	private boolean checkTools(Player player, ItemStack tool, int damageMultiplicand, int count){
		if(config.getTools().isPreserve()){
			var toolUsesLeft = tool.isDamageableItem() ? (tool.getMaxDamage() - tool.getDamageValue()) : Integer.MAX_VALUE;
			if(toolUsesLeft <= (damageMultiplicand * count)){
				player.sendMessage(new TranslatableComponent("chat.fallingtree.prevented_break_tool"), NIL_UUID);
				return false;
			}
		}
		return true;
	}
	
	private int breakPart(Tree tree, TreePart treePart, Level level, Player player, ItemStack tool, int damageMultiplicand){
		var blockPos = treePart.blockPos();
		var logState = level.getBlockState(blockPos);
		logState.getBlock().playerDestroy(level, player, tree.getHitPos(), logState, level.getBlockEntity(blockPos), tool);
		level.removeBlock(blockPos, false);
		return 1;
	}
	
	public static ShiftDownTreeBreakingHandler getInstance(){
		if(isNull(INSTANCE)){
			INSTANCE = new ShiftDownTreeBreakingHandler();
		}
		return INSTANCE;
	}
}
