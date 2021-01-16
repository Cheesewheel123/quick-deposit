package me.cheesewheel123.QuickDeposit;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class QuickDepositListener implements Listener {
    
    @EventHandler
    public void onShiftClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack stack = p.getInventory().getItemInMainHand();
        if(e.getClickedBlock() != null && e.getClickedBlock().getState() instanceof Chest &&
            p.isSneaking() && e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {                        //checks if the click is a shift-click on a chest
            InventoryHolder invHolder = (InventoryHolder) e.getClickedBlock().getState();
            Inventory inv = invHolder.getInventory();
            org.bukkit.block.data.type.Chest chestData = 
                (org.bukkit.block.data.type.Chest) e.getClickedBlock().getBlockData();
            if(p.hasPermission("quickdeposit.use")) {                                                 //checks if player has permission to use QuickDeposit
                if(!chestData.getType().equals(Type.SINGLE)) {                                        //checks if chest is a double chest
                    Block otherChest = findOtherInDoubleChest(chestData, e.getClickedBlock());        //finds other half of the double chest
                    org.bukkit.block.data.type.Chest otherChestData = 
                        (org.bukkit.block.data.type.Chest) otherChest.getBlockData();
                    if(otherChest == null || otherChest.getRelative(BlockFace.UP).getType().isSolid() || 
                        e.getClickedBlock().getRelative(BlockFace.UP).getType().isSolid() ) {         //checks if there is a solid block above either side of the double chest
                        p.sendMessage("Sorry, that chest is locked");
                    } else {
                        transferItems(inv, stack, p);                                                 //if not, deposits item stack in main hand in the chest
                    }
                    if(chestData.getType().equals(Type.LEFT)) {                                       //resets the double chest to its original appearance
                        chestData.setType(Type.LEFT);
                        otherChestData.setType(Type.RIGHT);
                    } else {
                        chestData.setType(Type.RIGHT);
                        otherChestData.setType(Type.LEFT);
                    }
                    otherChest.setBlockData(otherChestData);
                    e.getClickedBlock().setBlockData(chestData);
                } else if(e.getClickedBlock().getRelative(BlockFace.UP).getType().isSolid()){        //if not a double chest, checks if there is a solid block above it
                    p.sendMessage("Sorry, that chest is locked");
                    
                } else {
                    transferItems(inv, stack, p);                                                    //if not blocked, transfer the items
                }
            } else {
                p.sendMessage("Sorry, you don't have permission to do that");
            }  
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreakChest(BlockBreakEvent e) {                                                    //makes sure the chest doesn't break when you shift-click it
        Player p = e.getPlayer(); 
        if(p.isSneaking() && e.getBlock().getState() instanceof Chest) {
            e.setCancelled(true);
        }  
    }
    
    private void transferItems(Inventory inv, ItemStack stack, Player p) {                           //takes an item stack from a player and deposits it in a chest inventory
        if(stack != null && stack.getAmount() > 0) {                                                
            if(enoughRoom(inv, stack)) {                                                             //checks if there is enough room in the chest inventory
                ItemStack copyStack = stack.clone();                                                 
                stack.setAmount(0);                                                                  //removes the item stack from the player's inventory
                inv.addItem(copyStack);                                                              //adds the item stack to the chest inventory
                p.sendMessage("Your items have been deposited");
            } else {
                p.sendMessage("Sorry, there isn't enough room :(");
            } 
        }    
    }
    
    private boolean enoughRoom(Inventory inv, ItemStack stack) {                                     //checks if there is enough room in an inventory for an item stack
        int stackSize = stack.getAmount();
        for(ItemStack s : inv) {
            if(s == null) {
                return true;
            } else if(s.getType().equals(stack.getType())) {
                stackSize -= (stack.getMaxStackSize() - s.getAmount());
            }
        }
        return stackSize <= 0;
    }
    
    private Block findOtherInDoubleChest(org.bukkit.block.data.type.Chest chestData, Block chest) {  //Given one chest block of a double chest, it will find the other block
        BlockFace b = chestData.getFacing();
        switch (b) {
        case EAST:
            Block leftEast = chest.getRelative(BlockFace.NORTH);                                     //Finds the left and right sides of the original block
            Block rightEast = chest.getRelative(BlockFace.SOUTH);
            if(chestData.getType().equals(org.bukkit.block.data.type.Chest.Type.LEFT)) {             //Returns the other chest block based on the placement of the original block
                return rightEast;
            } else {
                return leftEast;
            }
        case NORTH:
            Block leftNorth = chest.getRelative(BlockFace.WEST);
            Block rightNorth = chest.getRelative(BlockFace.EAST);
            if(chestData.getType().equals(org.bukkit.block.data.type.Chest.Type.LEFT)) {
                return rightNorth;    
            } else {
                return leftNorth; 
            }
        case SOUTH:
            Block leftSouth = chest.getRelative(BlockFace.EAST);
            Block rightSouth = chest.getRelative(BlockFace.WEST);
            if(chestData.getType().equals(org.bukkit.block.data.type.Chest.Type.LEFT)) {
                return rightSouth;    
            } else {
                return leftSouth; 
            }
        case WEST:
            Block leftWest = chest.getRelative(BlockFace.SOUTH);
            Block rightWest= chest.getRelative(BlockFace.NORTH);
            if(chestData.getType().equals(org.bukkit.block.data.type.Chest.Type.LEFT)) {
                return rightWest;    
            } else {
                return leftWest; 
            }
        default:
            return null;
        }
    }
}
