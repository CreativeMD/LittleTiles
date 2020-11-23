package com.creativemd.littletiles.common.structure.signal.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.signal.output.SignalOutputCondition;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class SignalTicker {
	
	private static HashMap<World, SignalTicker> tickers = new HashMap<>();
	public static final int queueLength = 20;
	
	public static synchronized SignalTicker get(LittleStructure structure) {
		return get(structure.getWorld());
	}
	
	public static synchronized SignalTicker get(World world) {
		if (world.isRemote)
			throw new RuntimeException("Client should never ask for a signal ticker");
		if (world instanceof IOrientatedWorld)
			world = ((IOrientatedWorld) world).getRealWorld();
		SignalTicker ticker = tickers.get(world);
		if (ticker == null) {
			ticker = new SignalTicker(world);
			tickers.put(world, ticker);
		}
		return ticker;
	}
	
	public static ISignalScheduleTicket schedule(LittleStructure structure, SignalOutputCondition condition, boolean[] result, int tick) {
		return get(structure).openTicket(structure, condition, result, tick);
	}
	
	private static synchronized void unload(SignalTicker ticker) {
		MinecraftForge.EVENT_BUS.unregister(ticker);
		tickers.remove(ticker.world);
	}
	
	public final World world;
	private int queueIndex;
	private List<SignalScheduleTicket>[] queue;
	
	private List<SignalScheduleTicket> longQueue = new ArrayList<>();
	
	protected SignalTicker(World world) {
		this.world = world;
		queue = new List[queueLength];
		queueIndex = 0;
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public synchronized void tick(WorldTickEvent event) {
		if (event.phase == Phase.END && world == event.world) {
			// Process queue
			List<SignalScheduleTicket> tickets = queue[queueIndex];
			for (int i = 0; i < tickets.size(); i++)
				tickets.get(i).run();
			tickets.clear();
			longQueue.removeIf((x) -> {
				if (x.tick() <= queueLength) {
					x.enterShortQueue(queueIndex);
					tickets.add(x);
					return true;
				}
				return false;
			});
			queueIndex++;
			if (queueIndex >= queueLength)
				queueIndex = 0;
		}
	}
	
	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload event) {
		if (event.getWorld() == world)
			unload(this);
	}
	
	public int getDelayOfQueue(int index) {
		if (index >= queueIndex)
			return queueIndex - index + 1;
		return queueLength - index + queueIndex;
	}
	
	public synchronized List<ISignalScheduleTicket> findTickets(SignalOutputCondition condition) {
		List<ISignalScheduleTicket> tickets = new ArrayList<>();
		for (int i = 0; i < queue.length; i++)
			for (SignalScheduleTicket ticket : queue[i])
				if (ticket.is(condition))
					tickets.add(ticket);
		for (SignalScheduleTicket ticket : longQueue)
			if (ticket.is(condition))
				tickets.add(ticket);
		return tickets;
	}
	
	public synchronized ISignalScheduleTicket openTicket(LittleStructure structure, SignalOutputCondition condition, boolean[] result, int tick) {
		int delay;
		if (tick <= queueLength) {
			delay = queueIndex + tick - 1;
			if (delay >= queueLength)
				delay -= queueLength;
		} else
			delay = tick;
		SignalScheduleTicket ticket = new SignalScheduleTicket(condition, structure, result, delay);
		if (delay <= queueLength)
			longQueue.add(ticket);
		else
			queue[delay].add(ticket);
		return ticket;
	}
	
}
