package ru.rkfsociety;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.wbm.plugin.util.BlockTool;
import com.wbm.plugin.util.InventoryTool;
import com.wbm.plugin.util.LocationTool;
import com.wbm.plugin.util.MathTool;
import com.wbm.plugin.util.ParticleTool;
import com.wbm.plugin.util.SoundTool;
import com.worldbiomusic.minigameworld.minigameframes.SoloBattleMiniGame;
import com.worldbiomusic.minigameworld.minigameframes.helpers.MiniGameCustomOption.Option;



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
		data.put("block", Material.SNOW_BLOCK.name()); // Напольные блоки
		data.put("tool", Material.STONE_SHOVEL.name()); // Предмет выдаваемый игроку
		data.put("pos1", getLocation()); // пол
		data.put("pos2", getLocation()); // пол
		data.put("pos3", getLocation()); // пол
		data.put("pos4", getLocation()); // пол
	}

	@Override
	public void loadCustomData() {
		super.loadCustomData();

		Map<String, Object> data = getCustomData();
		this.block = Material.valueOf((String) data.get("block")); // Напольные блоки
		this.tool = Material.valueOf((String) data.get("tool")); // Предмет выдаваемый игроку
		this.pos1 = (Location) data.get("pos1"); // пол
		this.pos2 = (Location) data.get("pos2"); // пол
		this.pos3 = (Location) data.get("pos3"); // пол
		this.pos4 = (Location) data.get("pos4"); // пол
	}

	@Override
	protected void onStart() {
		super.onStart();

		getTaskManager().runTaskTimer("check-fallen", 0, 10);
		InventoryTool.addItemToPlayers(getPlayers(), new ItemStack(this.tool));
	}
	
	
	private void fillStage1() { // Заполнитель 1 этаж
		BlockTool.fillBlockWithMaterial(pos1, pos2, block);
	}
	
	private void fillStage2() { // Заполнитель 2 этаж
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
	
    public static void walls(Location pos1, Location pos4, Material block) {
        World world = pos1.getWorld();
        int pos1X = (int) pos1.getX();
        int pos2X = (int) pos4.getX();
        int pos1Y = (int) pos1.getY();
        int pos2Y = (int) pos4.getY();
        int pos1Z = (int) pos1.getZ();
        int pos2Z = (int) pos4.getZ();

        // разница
        int dx = MathTool.getDiff(pos1X, pos2X);
        int dy = MathTool.getDiff(pos1Y, pos2Y);
        int dz = MathTool.getDiff(pos1Z, pos2Z);

        // получение меньших x, y, z
        int smallX = Math.min(pos1X, pos2X);
        int smallY = Math.min(pos1Y, pos2Y);
        int smallZ = Math.min(pos1Z, pos2Z);

        // получение больших x, y, z
        int bigX = Math.max(pos1X, pos2X);
        int bigY = Math.max(pos1Y, pos2Y);
        int bigZ = Math.max(pos1Z, pos2Z);

        Location innerSmallPos = new Location(world, smallX + 1, smallY, smallZ + 1);
        Location innerBigPos = new Location(world, bigX - 1, bigY + 5, bigZ - 1);

        Location loc = new Location(world, smallX, smallY, smallZ);
        for (int y = 0; y <= dy; y++) {
            for (int z = 0; z <= dz; z++) {
                for (int x = 0; x <= dx; x++) {
                    loc.add(x, y, z);

                    // проверка внутренней границы
                    if (!LocationTool.isIn(innerSmallPos, loc, innerBigPos)) {
                        // set type
                        loc.getBlock().setType(block);
                    }

                    // init
                    loc.setX(smallX);
                    loc.setY(smallY);
                    loc.setZ(smallZ);
                }
            }
        }
    }
	
	@Override
	protected void initGame() {
		fillStage1();
		fillStage2();
	}
	@Override
	protected void onEvent(Event event) { // Отслеживание поломки
		if (event instanceof BlockBreakEvent) {
			BlockBreakEvent e = (BlockBreakEvent) event;

			Block block = e.getBlock();
			
			if(!(LocationTool.isIn(pos1, block.getLocation(), pos2) || LocationTool.isIn(pos3, block.getLocation(), pos4))) {
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