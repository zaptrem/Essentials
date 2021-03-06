package com.earth2me.essentials.commands;

import static com.earth2me.essentials.I18n._;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;


public class Commandtpaccept extends EssentialsCommand
{
	public Commandtpaccept()
	{
		super("tpaccept");
	}

	@Override
	public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception
	{

		final User target = user.getTeleportRequest();

		if (target == null || !target.isOnline())
		{
			throw new Exception(_("noPendingRequest"));
		}

		if (user.isTpRequestHere() && ((!target.isAuthorized("essentials.tpahere") && !target.isAuthorized("essentials.tpaall"))
									   || (user.getWorld() != target.getWorld() && ess.getSettings().isWorldTeleportPermissions()
										   && !user.isAuthorized("essentials.world." + user.getWorld().getName()))))
		{
			throw new Exception(_("noPendingRequest"));
		}

		if (!user.isTpRequestHere() && (!target.isAuthorized("essentials.tpa")
										|| (user.getWorld() != target.getWorld() && ess.getSettings().isWorldTeleportPermissions()
											&& !user.isAuthorized("essentials.world." + target.getWorld().getName()))))
		{
			throw new Exception(_("noPendingRequest"));
		}

		if (args.length > 0 && !target.getName().contains(args[0]))
		{
			throw new Exception(_("noPendingRequest"));
		}

		long timeout = ess.getSettings().getTpaAcceptCancellation();
		if (timeout != 0 && (System.currentTimeMillis() - user.getTeleportRequestTime()) / 1000 > timeout)
		{
			user.requestTeleport(null, false);
			throw new Exception(_("requestTimedOut"));
		}

		final Trade charge = new Trade(this.getName(), ess);
		if (user.isTpRequestHere())
		{
			charge.isAffordableFor(user);
		}
		else
		{
			charge.isAffordableFor(target);
		}
		user.sendMessage(_("requestAccepted"));
		target.sendMessage(_("requestAcceptedFrom", user.getDisplayName()));

		if (user.isTpRequestHere())
		{
			user.getTeleport().teleport(target, charge, TeleportCause.COMMAND);
		}
		else
		{
			target.getTeleport().teleport(user, charge, TeleportCause.COMMAND);
		}
		user.requestTeleport(null, false);
	}
}
