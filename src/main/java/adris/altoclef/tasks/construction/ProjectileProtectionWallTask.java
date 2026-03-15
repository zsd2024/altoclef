package adris.altoclef.tasks.construction;

import java.util.Optional;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.EntityHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 投射物防护墙任务 - 在玩家和敌对实体之间放置方块以阻挡投射物
 */
public class ProjectileProtectionWallTask extends Task implements ITaskRequiresGrounded {

	// AltoClef主模块实例
	private final AltoClef mod;
	// 方块放置等待计时器
    private final TimerGame waitForBlockPlacement = new TimerGame(2);

    // 目标放置位置
    private BlockPos targetPlacePos;
	
	/**
	 * 构造函数
	 * @param mod AltoClef主模块实例
	 */
	public ProjectileProtectionWallTask(AltoClef mod) {
		this.mod = mod;
	}
	
	@Override
	protected void onStart() {
		// 开始时强制计时器过期以立即执行任务
		waitForBlockPlacement.forceElapse();
	}

@Override
	protected Task onTick() {
		// 如果目标位置存在且尚未放置方块
		if (targetPlacePos != null && !WorldHelper.isSolidBlock(targetPlacePos)) {
			// 获取可丢弃的方块槽位
			Optional<adris.altoclef.util.slots.Slot> slot = StorageHelper.getSlotWithThrowawayBlock(this.mod, true);
			if(slot.isPresent()) {
				// 在目标位置放置方块
				place(targetPlacePos, Hand.MAIN_HAND, slot.get().getInventorySlot());
				// 清空目标位置并重置调试状态
				targetPlacePos = null;
				setDebugState(null);
			}
			return null;
		}

		// 寻找最近的敌对骷髅实体（正在使用弓箭的）
		Optional<Entity> sentity = mod.getEntityTracker().getClosestEntity((e) -> {
         	if(e instanceof SkeletonEntity 
         			&& EntityHelper.isAngryAtPlayer(mod, e)
         			&& 
         			(((SkeletonEntity) e).getItemUseTime() > 8)
         			) return true;
         	return false;
         }, SkeletonEntity.class);
         
         if(sentity.isPresent()) {
     		// 获取玩家和目标实体的位置
     		Vec3d playerPos = mod.getPlayer().getPos();
             Vec3d targetPos = sentity.get().getPos();
     		// 计算朝向目标实体的方向向量
             Vec3d direction = playerPos.subtract(targetPos).normalize();

             // 计算在实体方向上两格远的新位置
             double x = playerPos.x - 2 * direction.x;
             double y = playerPos.y + direction.y;
             double z = playerPos.z - 2 * direction.z;
             
             // 设置目标放置位置（向上一格）
             targetPlacePos = new BlockPos((int) x, (int) y+1, (int) z);
			setDebugState("正在 " + targetPlacePos.toString() + " 处放置方块");
			waitForBlockPlacement.reset();
         }
		return null;
	}

	@Override
	protected void onStop(Task interruptTask) {
		// 任务停止时无需特殊处理
	}
	
@Override
    public boolean isFinished() {
        assert MinecraftClient.getInstance().world != null;
        
        // 检查是否有敌对骷髅实体还在使用弓箭
        Optional<Entity> entity = mod.getEntityTracker().getClosestEntity((e) -> {
         	if(e instanceof SkeletonEntity 
         			&& EntityHelper.isAngryAtPlayer(mod, e)
         			&& 
         			(((SkeletonEntity) e).getItemUseTime() > 3)
         			) return true;
         	return false;
         }, SkeletonEntity.class);
        
        // 如果目标位置已放置方块或没有敌对实体则完成任务
        return targetPlacePos != null && WorldHelper.isSolidBlock(targetPlacePos) || entity.isEmpty();
    }

	@Override
	protected boolean isEqual(Task other) {
		// 暂未实现任务相等性比较，返回true表示相等
		return other instanceof ProjectileProtectionWallTask;
	}

	@Override
	protected String toDebugString() {
		return "放置方块以阻挡投射物";
	}
	
	/**
 * 获取合适的方块放置面
 * @param blockPos 目标方块位置
 * @return 合适的放置面方向，如果找不到则返回null
 */
public Direction getPlaceSide(BlockPos blockPos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbor = blockPos.offset(side);
            BlockState state = mod.getWorld().getBlockState(neighbor);

            // 检查邻居方块是否为空气或可点击方块
            if (state.isAir() || isClickable(state.getBlock())) continue;

            // 检查邻居方块是否为流体
            if (!state.getFluidState().isEmpty()) continue;

            return side;
        }

        return null;
    }
	
	/**
 * 在指定位置放置方块
 * @param blockPos 目标方块位置
 * @param hand 使用的手
 * @param slot 方块所在的槽位
 * @return 放置是否成功
 */
public boolean place(BlockPos blockPos, Hand hand, int slot) {
        if (slot < 0 || slot > 8) return false;
        if (!canPlace(blockPos)) return false;

        Vec3d hitPos = Vec3d.ofCenter(blockPos);

        BlockPos neighbour;
        Direction side = getPlaceSide(blockPos);

        if (side == null) {
        	// 如果找不到合适的面，则尝试在下方放置
        	place(blockPos.down(), hand, slot);
        	return false;
        } else {
            neighbour = blockPos.offset(side);
            hitPos = hitPos.add(side.getOffsetX() * 0.5, side.getOffsetY() * 0.5, side.getOffsetZ() * 0.5);
        }

        BlockHitResult bhr = new BlockHitResult(hitPos, side.getOpposite(), neighbour, false);

        // 设置玩家视角
        mod.getPlayer().setYaw((float) getYaw(hitPos));
        mod.getPlayer().setPitch((float) getPitch(hitPos));
		// 切换到指定槽位
		swap(slot);

        interact(bhr, hand);


        return true;
    }
    
	
	/**
 * 检查方块是否可点击（不能在其旁边放置方块）
 * @param block 要检查的方块
 * @return 如果方块可点击则返回true
 */
public static boolean isClickable(Block block) {
        return block instanceof CraftingTableBlock
            || block instanceof AnvilBlock
            || block instanceof ButtonBlock
            || block instanceof AbstractPressurePlateBlock
            || block instanceof BlockWithEntity
            || block instanceof BedBlock
            || block instanceof FenceGateBlock
            || block instanceof DoorBlock
            || block instanceof NoteBlock
            || block instanceof TrapdoorBlock;
    }
	
	/**
 * 与方块交互（放置方块）
 * @param blockHitResult 方块命中结果
 * @param hand 使用的手
 */
public void interact(BlockHitResult blockHitResult, Hand hand) {
        // 临时取消潜行状态
        boolean wasSneaking = mod.getPlayer().input.sneaking;
        mod.getPlayer().input.sneaking = false;

        ActionResult result = mod.getController().interactBlock(mod.getPlayer(),hand, blockHitResult);

        if (result.shouldSwingHand()) {
            // 如果需要挥动手臂则执行
            mod.getPlayer().swingHand(hand);
        }

        // 恢复潜行状态
        mod.getPlayer().input.sneaking = wasSneaking;
    }

	/**
 * 检查是否可以在指定位置放置方块
 * @param blockPos 目标方块位置
 * @param checkEntities 是否检查实体占用
 * @return 是否可以放置
 */
public boolean canPlace(BlockPos blockPos, boolean checkEntities) {
        if (blockPos == null) return false;

        // 检查Y轴层级
        if (!World.isValid(blockPos) || !AltoClef.getInstance().getWorld().isInBuildLimit(blockPos)) return false;

        // 检查当前方块是否可替换
        if (!mod.getWorld().getBlockState(blockPos).isReplaceable()) return false;

        // 检查是否与实体冲突
        return !checkEntities || mod.getWorld().canPlace(Blocks.OBSIDIAN.getDefaultState(), blockPos, ShapeContext.absent());
    }

/**
 * 检查是否可以在指定位置放置方块（包含实体检查）
 * @param blockPos 目标方块位置
 * @return 是否可以放置
 */
    public boolean canPlace(BlockPos blockPos) {
        return canPlace(blockPos, true);
    }
	
    /**
 * 切换到指定槽位
 * @param slot 要切换到的槽位
 * @return 是否切换成功
 */
public boolean swap(int slot) {
        if (slot == PlayerSlot.OFFHAND_SLOT.getInventorySlot()) return true;
        if (slot < 0 || slot > 8) return false;

        mod.getPlayer().getInventory().selectedSlot = slot;
        return true;
    }
    
/**
 * 计算朝向目标位置的偏航角
 * @param pos 目标位置
 * @return 计算得到的偏航角
 */
    public double getYaw(Vec3d pos) {
        return mod.getPlayer().getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(pos.getZ() - mod.getPlayer().getZ(), pos.getX() - mod.getPlayer().getX())) - 90f - mod.getPlayer().getYaw());
    }

/**
 * 计算朝向目标位置的俯仰角
 * @param pos 目标位置
 * @return 计算得到的俯仰角
 */
    public double getPitch(Vec3d pos) {
        double diffX = pos.getX() - mod.getPlayer().getX();
        double diffY = pos.getY() - (mod.getPlayer().getY() + mod.getPlayer().getEyeHeight(mod.getPlayer().getPose()));
        double diffZ = pos.getZ() - mod.getPlayer().getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return mod.getPlayer().getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mod.getPlayer().getPitch());
    }
}