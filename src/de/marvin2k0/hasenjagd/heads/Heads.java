package de.marvin2k0.hasenjagd.heads;

import org.bukkit.inventory.ItemStack;

public enum Heads
{
    RABBIT("ZThhNGZiMjVhZmJjNmI3YWVjMTVhYmU4NzJmY2VhZDFlNWIzM2MxYWIxMjUyNTE0MWQ3N2JmZDI5OGZjMzJkOSJ9fX0==", "rabbit"),
    HUNTER("MzY4ZDVkMmViOGQ1ZDAxZWNiODgzMjVlNmI1NmQ4N2QwYjliZTQxNzIxZjEyOGY4OWJhNWYxMzBhYWU4OWZhIn19fQ==","hunter");

    private ItemStack item;
    private String idTag;
    private String prefix = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv";

    private Heads(String texture, String id)
    {
        item = Main.createSkull(prefix + texture, id);
        idTag = id;
    }


    public ItemStack getItemStack()
    {
        return item;
    }

    public String getName()
    {
        return idTag;
    }


}