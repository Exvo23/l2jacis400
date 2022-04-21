package net.sf.l2j.gameserver.model.entity.events.partyfarm;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminPartyFarm;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.events.deathmatch.DMEvent;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;


public class PartyFarm
{
	public static L2Spawn _monster;
	public static int _bossHeading = 0;
	public static String _eventName = "";
	public static boolean _started = false;
	public static boolean _aborted = false;
	protected static boolean _finish = false;
	static PartyFarm _instance;

	public static void bossSpawnMonster()
	{
		spawnMonsters();
		DMEvent.sysMsgToAllParticipants("Teleport Now! " + Config.PARTY_FARMANNONCER);
		DMEvent.sysMsgToAllParticipants("[Party Farm]: Duration: " + Config.EVENT_BEST_FARM_TIME + " minute(s)!");

		_aborted = false;
		_started = true;

		waiter(Config.EVENT_BEST_FARM_TIME * 60 * 1000);
		if (!_aborted)
			Finish_Event();
		unSpawnMonsters();
	}

	public static void Finish_Event()
	{
		unSpawnMonsters();

		_started = false;
		_finish = true;

		DMEvent.sysMsgToAllParticipants("[Party Farm]: Finished!");
		if (!AdminPartyFarm._bestfarm_manual)
			PartyFarmEvent.getInstance().StartCalculationOfNextEventTime();
		else
			AdminPartyFarm._bestfarm_manual = false;
	}

	public static void spawnMonsters()
	{
		for (int i = 0; i < Config.MONSTER_LOCS_COUNT; i++)
		{
			int[] coord = Config.MONSTER_LOCS[i];
			monstersArea.add(spawnNPC(coord[0], coord[1], coord[2], Config.monsterId));
			
		
		}
	}
	

	public static boolean is_started()
	{
		return _started;
	}

	public static boolean is_finish()
	{
		return _finish;
	}

	protected static L2Spawn spawnNPC(int xPos, int yPos, int zPos, int npcId)
	{
		NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
		try
		{
			final L2Spawn spawn = new L2Spawn(template);
			spawn.setLocx(xPos);
			spawn.setLocy(yPos);
			spawn.setLocz(zPos);
			spawn.setHeading(0);
			spawn.setRespawnDelay(3);
			SpawnTable.getInstance().addNewSpawn(spawn, false);
			spawn.init();
		
			spawn.getLastSpawn().setTitle("Event Champion");
			spawn.getLastSpawn().isAggressive();
			spawn.getLastSpawn().decayMe();
			spawn.getLastSpawn().spawnMe(spawn.getLastSpawn().getX(), spawn.getLastSpawn().getY(), spawn.getLastSpawn().getZ());
			spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), spawn.getLastSpawn(), 1034, 1, 1, 1));

			return spawn;
		}
		catch (Exception e)
		{
		}
		return null;
	}
	
    

public static List<L2Spawn> monstersArea = new CopyOnWriteArrayList<>();



	protected static void unSpawnMonsters()
	{
		for (L2Spawn s : monstersArea)
		{
			if (s == null)
			{
				monstersArea.remove(s);
				continue;
			}
			
			s.getLastSpawn().deleteMe();
			s.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(s, true);
			monstersArea.remove(s);
		}
	}

	protected static void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000L);
		while (startWaiterTime + interval > System.currentTimeMillis() && !_aborted)
		{
			seconds--;
			switch (seconds)
			{
				case 3600:
					if (_started)
						
						
					
						DMEvent.sysMsgToAllParticipants("[Party Farm]: " + seconds / 60 / 60 + " Time event finish!");
					
					break;
				case 60:
				case 120:
				case 180:
				case 240:
				case 300:
				case 600:
				case 900:
				case 1800:
					if (_started)
						DMEvent.sysMsgToAllParticipants("[Party Farm]: " + seconds / 60 + " minute(s) event finish!");
					break;
				case 1:
				case 2:
				case 3:
				case 10:
				case 15:
				case 30:
					if (_started)
						DMEvent.sysMsgToAllParticipants("[Party Farm]: " + seconds + " second(s) event finish!");
					
					
					
					break;
			}
			long startOneSecondWaiterStartTime = System.currentTimeMillis();
			while (startOneSecondWaiterStartTime + 1000L > System.currentTimeMillis())
				try
			{
					Thread.sleep(1L);
			}
			catch (InterruptedException ie)
			{
				ie.printStackTrace();
			}
		}
	}

}
