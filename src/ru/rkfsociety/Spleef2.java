package ru.rkfsociety;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.wbm.plugin.util.BlockTool;
import com.wbm.plugin.util.InventoryTool;
import com.wbm.plugin.util.LocationTool;
import com.wbm.plugin.util.ParticleTool;
import com.wbm.plugin.util.SoundTool;
import com.worldbiomusic.minigameworld.minigameframes.SoloBattleMiniGame;
import com.worldbiomusic.minigameworld.minigameframes.helpers.MiniGameCustomOption.Option;

/**
 * Spleef <br>
 * - SoloBattle <br>
 * - Check fallen from floor with timer task<br>
 * - Player will fall if below of y of custom-data.pos2<br>
 * - Gets score when break a block <br>
 * - Can set floor area with custom-data <br>
 * - Can set block, tool with custom-data <br>
 * - Fill floor in initGame() <br>
 * - PVP off <br>
 *
 */
public class Spleef2 extends SoloBattleMiniGame {

	private Material block;
	private Material tool;
	private Location pos1, pos2, pos3, pos4;

	public Spleef2() {
		super("Spleef2", 2, 10, 300, 20);

		getSetting().setIcon(Material.STONE_SHOVEL);

		getCustomOption().set(Option.COLOR, ChatColor.WHITE);
		getCustomOption().set(Option.PVP, false);
		getCustomOption().set(Option.PVE, false);
		getCustomOption().set(Option.PLAYER_HURT, false);

		registerTask();
	}

	private void registerTask() {
		getTaskManager().registerTask("check-fallen", () -> {
			getLivePlayers().forEach(p -> checkFallenFromFloor(p));
		});
	}

	@Override
	protected void initCustomData() {
		super.initCustomData();

		Map<String, Object> data = getCustomData();
		data.put("floorblock", Material.SNOW_BLOCK.name()); // ��������� �����
		data.put("tool", Material.STONE_SHOVEL.name()); // ������� ���������� ������
		data.put("pos1", getLocation());
		data.put("pos2", getLocation());
		data.put("pos3", getLocation());
		data.put("pos4", getLocation());
	}

	@Override
	public void loadCustomData() {
		super.loadCustomData();

		Map<String, Object> data = getCustomData();
		this.block = Material.valueOf((String) data.get("floorblock")); // ��������� �����
		this.tool = Material.valueOf((String) data.get("tool")); // ������� ���������� ������
		this.pos1 = (Location) data.get("pos1");
		this.pos2 = (Location) data.get("pos2");
		this.pos3 = (Location) data.get("pos3");
		this.pos4 = (Location) data.get("pos4");
	}

	@Override
	protected void onStart() {
		super.onStart();

		getTaskManager().runTaskTimer("check-fallen", 0, 10);
		InventoryTool.addItemToPlayers(getPlayers(), new ItemStack(this.tool));
	}

	private void fillStage1() {
		BlockTool.fillBlockWithMaterial(pos1, pos2, block);
	}
	
	private void fillStage2() {
		BlockTool.fillBlockWithMaterial(pos3, pos4, block);
	}


	private void checkFallenFromFloor(Player p) {
		double bottomY = this.pos2.getY();
		double playerY = p.getLocation().getY();

		if (playerY <= bottomY) {
			sendTitle(p, ChatColor.RED + "DIE", "");
			sendMessages(ChatColor.RED + p.getName() + ChatColor.RESET + " died");
			SoundTool.play(getPlayers(), Sound.BLOCK_BELL_USE);
			ParticleTool.spawn(p.getLocation(), Particle.FLAME, 30, 0.1);
			setLive(p, false);
		}
	}
	@Override
	protected void initGame() {
		fillStage1();
		fillStage2();
	}
	@Override
	protected void onEvent(Event event) { // ������������ �������
		if (event instanceof BlockBreakEvent) {
			BlockBreakEvent e = (BlockBreakEvent) event;

			Block block = e.getBlock();
			
			if (!LocationTool.isIn(pos1, block.getLocation(), pos2)) {
				return;
			}
			
			if (!LocationTool.isIn(pos3, block.getLocation(), pos4)) {
				return;
			}

			Material blockType = block.getType();
			if (blockType != this.block) {
				return;
			}

			Player p = e.getPlayer();
			plusScore(p, 1);
			block.setType(Material.AIR);
		}
	}
	
	@Override
	protected List<String> tutorial() {
		List<String> tutorial = new ArrayList<>();
		tutorial.add("Break block: " + ChatColor.GREEN + "+1");
		tutorial.add("Fallen: " + ChatColor.RED + "die");
		return tutorial;
	}

}