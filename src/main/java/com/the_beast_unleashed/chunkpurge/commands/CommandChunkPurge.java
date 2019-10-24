package com.the_beast_unleashed.chunkpurge.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.the_beast_unleashed.chunkpurge.ModChunkPurge;
import com.the_beast_unleashed.chunkpurge.events.HandlerWorldTick;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

// I attempted to heavily comment this code more than necessary in case anybody
// in the future wants to edit the code and wonders what the heck I was doing

public class CommandChunkPurge extends CommandBase
{
	// Use msg to format text to send to the player
	ITextComponent msg;
	// Set the mod's command name, used as /commandName [Other cool secondary commands]
  @Override
  public String getName()
  {
    return "chunkpurge";
  }
  // Tell the player what the command does in the /help menu
  @Override
  public String getUsage(ICommandSender commandUseage)
  {
    return "/chunkpurge | Get usage help for ChunkPurge";
  }
  
  //#####################################################
  // If you trigger these methods, you clearly did something wrong.
  private void invalid(ICommandSender commandInvalid)
  {
	  msg = new TextComponentString(TextFormatting.RED + "Invalid Argument. Type /chunkpurge for help");
	  commandInvalid.sendMessage(msg);
	  return;  
  }
  private void dimIntInvalid(ICommandSender commandInvalid)
  {
	  msg = new TextComponentString(TextFormatting.RED + "Dimensions MUST be an Integer. If more than one, they MUST");
		commandInvalid.sendMessage(msg);
		msg = new TextComponentString(TextFormatting.RED + "be comma seperated with no spaces");
		commandInvalid.sendMessage(msg);
		return;  
  }
  //#####################################################
  // If a command is entered, what should I do?
  @Override
  public void execute(MinecraftServer server, ICommandSender sender, String[] arg)
  {
	List<String> ARList;
	List<String> configIDs;
	int iAR;
	int length;
	String AR;
	String sAR;
	switch (arg.length){
	// Case 0 (no secondary commands) Lets tell the player how to use the command
	case 0:
		msg = new TextComponentString(TextFormatting.GREEN + "--- Showing Help for ChunkPurge ---");
		sender.sendMessage(msg);
		msg = new TextComponentString("/chunkpurge delay [ticks]");
		sender.sendMessage(msg);
		msg = new TextComponentString("/chunkpurge debug [true|false]");
		sender.sendMessage(msg);
		msg = new TextComponentString("/chunkpurge enable [true|false]");
		sender.sendMessage(msg);
		msg = new TextComponentString("/chunkpurge pradius [# of chunks]");
		sender.sendMessage(msg);
		msg = new TextComponentString("/chunkpurge tradius [# of chunks]");
		sender.sendMessage(msg);
		msg = new TextComponentString("/chunkpurge sradius [# of chunks]");
		sender.sendMessage(msg);
		msg = new TextComponentString("/chunkpurge dimlist | Lists dimensions that get purged");
		sender.sendMessage(msg);
		msg = new TextComponentString("/chunkpurge dimlist add [dimID,dimID,ect.]");
		sender.sendMessage(msg);
		msg = new TextComponentString("/chunkpurge dimlist remove [dimID,dimID,ect.]");
		sender.sendMessage(msg);
		return;
	// Case 1 (only one secondary command) The player must only want information about a command, not to change it
	case 1:
		// If they typed delay, lets give them the current ChunkPurge Unload Delay
		  if (arg[0].equalsIgnoreCase("delay"))
			{

				msg = new TextComponentString(TextFormatting.GREEN + "Unload Delay: " + TextFormatting.WHITE + ModChunkPurge.config.chunkUnloadDelay);
				sender.sendMessage(msg);
				return;
			}
		// If they typed debug, tell them if debug is on or not
		  if (arg[0].equalsIgnoreCase("debug"))
			{

				msg = new TextComponentString(TextFormatting.GREEN + "Debug Mode Enabled: " + TextFormatting.WHITE + ModChunkPurge.config.debug);
				sender.sendMessage(msg);
				return;
			}
		// If they typed enable, tell them if the mod is enabled or not 
		  if (arg[0].equalsIgnoreCase("enable"))
			{

				msg = new TextComponentString(TextFormatting.GREEN + "Chunk Purge Enabled: " + TextFormatting.WHITE + ModChunkPurge.config.enabled);
				sender.sendMessage(msg);
				return;
			}
		// If they typed pradius, give them the added radius to the players view range that will be ignored while unloading chunks
		  if (arg[0].equalsIgnoreCase("pradius"))
			{

				msg = new TextComponentString(TextFormatting.GREEN + "Player Ignore Radius = View Distance(" + server.getPlayerList().getViewDistance()+ ") + " + TextFormatting.WHITE + ModChunkPurge.config.pradius + TextFormatting.GREEN + " Chunks");
				sender.sendMessage(msg);
				return;
			}
		// If they typed tradius, give them the radius around chunk loaders (tickets) that will be ignored while unloading chunks
		  if (arg[0].equalsIgnoreCase("tradius"))
			{

				msg = new TextComponentString(TextFormatting.GREEN + "Chunk Loader Ticket Ignore Radius = " + TextFormatting.WHITE + ModChunkPurge.config.tradius + TextFormatting.GREEN + " Chunks");
				sender.sendMessage(msg);
				return;
			}
		 // If they typed sradius, give them the added radius to the Spawn Chunks that will be ignored while unloading chunks
		  if (arg[0].equalsIgnoreCase("sradius"))
			{

				msg = new TextComponentString(TextFormatting.GREEN + "Spawn Chunk Ignore Radius = Spawn Radius(8) + " + TextFormatting.WHITE + ModChunkPurge.config.sradius + TextFormatting.GREEN + " Chunks");
				sender.sendMessage(msg);
				return;
			}
		  // If they typed dimlist, give them the list of dimensions being processed by ChunkPurge
		  if (arg[0].equalsIgnoreCase("dimlist"))
			{
			  msg = new TextComponentString(TextFormatting.GREEN + "ID's of dimensions being ChunkPurged | " + TextFormatting.WHITE + ModChunkPurge.config.dimlist.replace(",",", "));
			  sender.sendMessage(msg);
			  return;
			}
		  // If none of the above, you did something wrong.
		  else
		  {
				invalid(sender);
				return;
		  }
	// Case 2 (The player typed in 2 separate commands after the main command) They must want to change a config value.
	case 2:
			// If they typed delay, either change the delay to what they typed, or catch an error and tell them what they entered is wrong.
			if (arg[0].equalsIgnoreCase("delay"))
			{
				try
				{
					
					if (Integer.valueOf(arg[1]) <= 0)
					{
						

						msg = new TextComponentString(TextFormatting.RED + "[Ticks] must be an integer greater than 0");
						sender.sendMessage(msg);
						return;
					}
					
					else
					{
						
						ModChunkPurge.config.chunkUnloadDelay = Integer.valueOf(arg[1]);

						msg = new TextComponentString(TextFormatting.GREEN + "Chunk Unload Delay Set to: " + TextFormatting.WHITE + arg[1]);
						sender.sendMessage(msg);
						
						ModChunkPurge.config.saveConfig();
						return;
					}
					
				}
				
				catch (NumberFormatException ex)
				{

					msg = new TextComponentString(TextFormatting.RED + "Chunk Unload Delay MUST be an integer. Your input was: " + TextFormatting.WHITE + arg[1]);
					sender.sendMessage(msg);
					
				}
				return;	
			}
			// If they typed debug, either change debug to true/false, or catch an error and tell them what they entered is wrong.
			if (arg[0].equalsIgnoreCase("debug"))
			{
				
				if (arg[1].equalsIgnoreCase("true") || arg[1].equalsIgnoreCase("false"))
				{
					
					ModChunkPurge.config.debug = Boolean.valueOf(arg[1]);

					msg = new TextComponentString(TextFormatting.GREEN + "Debug Mode Enabled Set to: " + TextFormatting.WHITE + ModChunkPurge.config.debug);
					sender.sendMessage(msg);
					
					ModChunkPurge.config.saveConfig();
					return;
				}
				
				else
				{

					msg = new TextComponentString(TextFormatting.RED + "Debug MUST be True or False. Your input was: " + TextFormatting.WHITE + arg[1]);
					sender.sendMessage(msg);
					return;
				}
			}
			// If they typed enable, either change enable to true/false, or catch an error and tell them what they entered is wrong.
			if (arg[0].equalsIgnoreCase("enable"))
			{
				
				if (arg[1].equalsIgnoreCase("true") || arg[1].equalsIgnoreCase("false"))
				{
					
					ModChunkPurge.config.enabled = Boolean.valueOf(arg[1]);

					msg = new TextComponentString(TextFormatting.GREEN + "Chunk Purge Enabled Set to: " + TextFormatting.WHITE + ModChunkPurge.config.enabled);
					sender.sendMessage(msg);
					
					ModChunkPurge.config.saveConfig();
					return;
				}
				
				else
				{

					msg = new TextComponentString(TextFormatting.RED + "Enabled MUST be True or False. Your input was: " + TextFormatting.WHITE + arg[1]);
					sender.sendMessage(msg);
					return;
				}
			}
			// If they typed pradius, either change the player ignore radius adder to what they typed, or catch an error and tell them what they entered is wrong.
			if (arg[0].equalsIgnoreCase("pradius"))
			{
				try
				{
					
					if (Integer.valueOf(arg[1]) < 0)
					{
						

						msg = new TextComponentString(TextFormatting.RED + "[# of chunks] MUST be an integer greater than or equal to 0");
						sender.sendMessage(msg);
						return;
					}
					
					else
					{
						
						ModChunkPurge.config.pradius = Integer.valueOf(arg[1]);

						msg = new TextComponentString(TextFormatting.GREEN + "Player Ignore Radius Set to: View Distance(" + server.getPlayerList().getViewDistance()+ ") + " + TextFormatting.WHITE + arg[1] + TextFormatting.GREEN + " Chunks");
						sender.sendMessage(msg);
						
						ModChunkPurge.config.saveConfig();
						return;
					}
					
				}
				
				catch (NumberFormatException ex)
				{

					msg = new TextComponentString(TextFormatting.RED + "[# of chunks] MUST be an integer greater than or equal to 0. " + TextFormatting.RED + "Your input was: " + TextFormatting.WHITE + arg[1]);
					sender.sendMessage(msg);
					
				}
				return;	
			}
			// If they typed tradius, either change the ticket ignore radius adder to what they typed, or catch an error and tell them what they entered is wrong.
			if (arg[0].equalsIgnoreCase("tradius"))
			{
				try
				{
					
					if (Integer.valueOf(arg[1]) < 0)
					{
						

						msg = new TextComponentString(TextFormatting.RED + "[# of chunks] MUST be an integer greater than or equal to 0");
						sender.sendMessage(msg);
						return;
					}
					
					else
					{
						
						ModChunkPurge.config.tradius = Integer.valueOf(arg[1]);

						msg = new TextComponentString(TextFormatting.GREEN + "Chunk Loader Ticket Ignore Radius Set to: " + TextFormatting.WHITE + arg[1] + TextFormatting.GREEN + " Chunks");
						sender.sendMessage(msg);
						
						ModChunkPurge.config.saveConfig();
						return;
					}
					
				}
				
				catch (NumberFormatException ex)
				{

					msg = new TextComponentString(TextFormatting.RED + "[# of chunks] MUST be an integer greater than or equal to 0. " + TextFormatting.RED + "Your input was: " + TextFormatting.WHITE + arg[1]);
					sender.sendMessage(msg);
					
				}
				return;	
			}
			// If they typed sradius, either change the spawn chunk ignore radius adder to what they typed, or catch an error and tell them what they entered is wrong.
			if (arg[0].equalsIgnoreCase("sradius"))
			{
				try
				{
					
					if (Integer.valueOf(arg[1]) < 0)
					{
						

						msg = new TextComponentString(TextFormatting.RED + "[# of chunks] MUST be an integer greater than or equal to 0");
						sender.sendMessage(msg);
						return;
					}
					
					else
					{
						
						ModChunkPurge.config.sradius = Integer.valueOf(arg[1]);

						msg = new TextComponentString(TextFormatting.GREEN + "Spawn Chunk Ignore Radius Set to: Spawn Radius(8) + " + TextFormatting.WHITE + arg[1] + TextFormatting.GREEN + " Chunks");
						sender.sendMessage(msg);
						
						ModChunkPurge.config.saveConfig();
						return;
					}
					
				}
				
				catch (NumberFormatException ex)
				{

					msg = new TextComponentString(TextFormatting.RED + "[# of chunks] MUST be an integer greater than or equal to 0. " + TextFormatting.RED + "Your input was: " + TextFormatting.WHITE + arg[1]);
					sender.sendMessage(msg);
					
				}
				return;	
			}
			// If none of the above, you did something wrong
			else
			{
				invalid(sender);
				return;
			}
	// Case 3 (3 separate commands were typed after the main command) They must want to add to or remove from the dimlist
	case 3:
		// Their first command had better be dimlist
		if(arg[0].equalsIgnoreCase("dimlist"))
		{
			// If they type add as the second command, lets add something to the dimlist or catch an error if they enter that something wrong
			if(arg[1].equalsIgnoreCase("add"))
			{
				try
				{
					// Turn the player input into an array
					ARList = Arrays.asList(arg[2].split(","));
					// Check the size of the array
					length = ARList.size();
					// Process the following for each object in the array
					for (int i=0; i<length; i++)
					{
						// Set the object to String first. We could just use iAR = Integer.parseInt(String.valueOf(ARList.get(i)));
						// but we use the string later, so this just makes everything cleaner.
						sAR = ARList.get(i);
						// Attempt to turn the string into an integer (this is where exception is caught)
						iAR = Integer.parseInt(sAR);
						// If you get down here, it's a valid number, so lets call the current dimlist to do things with it
						configIDs = Arrays.asList(ModChunkPurge.config.dimlist.split(","));
						if(configIDs.contains(sAR))
						{
							// Why are you adding things to the dimlist that are already there... why do I have to write code to catch this?
							msg = new TextComponentString(TextFormatting.RED + "Config already contains dimension " + TextFormatting.WHITE + sAR);
							sender.sendMessage(msg);
						}
						else
						{
						// Add the new input to the dimlist, reset the dimlist value, and tell the player
						ModChunkPurge.config.dimlist = ModChunkPurge.config.dimlist + "," + iAR;
						msg = new TextComponentString(TextFormatting.GREEN + "Dimension " + TextFormatting.WHITE + sAR + TextFormatting.GREEN + " added to config");
						sender.sendMessage(msg);
						}
						
					}
					  msg = new TextComponentString(TextFormatting.GREEN + "ID's of dimensions now being ChunkPurged | " + TextFormatting.WHITE + ModChunkPurge.config.dimlist.replace(",",", "));
					  sender.sendMessage(msg);
					// SAVE your config AFTER the loop, not inside it.
					ModChunkPurge.config.saveConfig();
					return;
				}
				catch (NumberFormatException ex)
				{
					dimIntInvalid(sender);
					return;
				}
			}
			// If they type remove, lets remove things or catch an error and tell them what they typed is wrong
			if(arg[1].equalsIgnoreCase("remove"))
			{
				try
				{
					//Turn the player input into an array
					ARList = Arrays.asList(arg[2].split(","));
					//Check the size of the array
					length = ARList.size();
					// Process the following for each object in the array
					for (int i=0; i<length; i++)
					{
						// Set the object to String first. We could just use iAR = Integer.parseInt(String.valueOf(ARList.get(i)));
						// but we use the string later, so this just makes everything cleaner.
						sAR = ARList.get(i);
						// Attempt to turn the string into an integer (this is where exception is caught)
						iAR = Integer.parseInt(sAR);
						// If you get down here, it's a valid number, so lets call the current dimlist to do things with it
						configIDs = Arrays.asList(ModChunkPurge.config.dimlist.split(","));
						if(configIDs.contains(sAR))
						{
							// Grab the dimlist
							AR = ModChunkPurge.config.dimlist;
							// Replace the removed dim with nothing
							AR = AR.replace(sAR, "");
							// Clean up the comma mess you made
							AR = AR.replace(",,", ",");
							AR = AR.replaceFirst("^,", "");
							AR = AR.replaceAll(",$", "");
							// Set the new dimlist and tell the player they did a good job
							ModChunkPurge.config.dimlist = AR;
							msg = new TextComponentString(TextFormatting.GREEN + "Removed Dimension " + TextFormatting.WHITE + sAR + TextFormatting.GREEN + " from Dimensions being ChunkPurged");
							sender.sendMessage(msg);
						}
						else
						{
							// Why are you trying to remove things that aren't there? Stop doing that...
							msg = new TextComponentString(TextFormatting.RED + "Dimension " + TextFormatting.WHITE + sAR + TextFormatting.RED + " was not in the config");
							sender.sendMessage(msg);
						}
						
					}
					msg = new TextComponentString(TextFormatting.GREEN + "ID's of dimensions now being ChunkPurged | " + TextFormatting.WHITE + ModChunkPurge.config.dimlist.replace(",",", "));
					  sender.sendMessage(msg);
					// SAVE your config AFTER the loop, not inside it.
					ModChunkPurge.config.saveConfig();
					return;
				}
				catch (NumberFormatException ex)
				{
					dimIntInvalid(sender);
					return;
				}
			}
			// Look, 3 more instances where the player typed something wrong
			else
			{
				invalid(sender);
				return;
			}
		}
		else
		{
			invalid(sender);
			return;
		}
	default:
		invalid(sender);
		return;
	}
  }

  // Set the permission for the command higher than the normal user. Normal user is 1? Default OP is 4.
  @Override
  public int getRequiredPermissionLevel() {
      return 2;
  }
}