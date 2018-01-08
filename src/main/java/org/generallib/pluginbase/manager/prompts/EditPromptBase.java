package org.generallib.pluginbase.manager.prompts;

import org.bukkit.Bukkit;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.generallib.pluginbase.PluginBase;

public abstract class EditPromptBase implements Prompt{
    protected static final int CONTENTSLINE_PER_PAGE = 8;

    protected final Prompt parent;
    protected final String title;
    public EditPromptBase(Prompt parent, String title) {
        this.parent = parent;
        this.title = title;
    }
    public Prompt getParent() {
        return parent;
    }
    public String getTitle() {
        return title;
    }

    @Override
    public String getPromptText(ConversationContext arg0) {
        if(!(arg0.getPlugin() instanceof PluginBase))
            return null;

        PluginBase base = (PluginBase) arg0.getPlugin();

        Bukkit.getScheduler().runTask(base, new Runnable() {
            @Override
            public void run() {
                Conversable conv = arg0.getForWhom();

                print(conv);
            }
        });
        return null;
    }

    protected void print(Conversable conv) {
        //clean up screen
        for(int i = 0; i < 10; i++)
            conv.sendRawMessage("");

        conv.sendRawMessage("<"+title+">");
        conv.sendRawMessage("");
    }
}
