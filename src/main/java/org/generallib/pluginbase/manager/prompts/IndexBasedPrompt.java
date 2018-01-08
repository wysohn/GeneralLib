package org.generallib.pluginbase.manager.prompts;

import java.util.List;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public abstract class IndexBasedPrompt<T> extends EditPromptBase {
    private final List<T> currentData;

    private int currentIndex = 0;

    public IndexBasedPrompt(Prompt parent, String title, List<T> currentData) {
        super(parent, title);
        this.currentData = currentData;
    }

    @Override
    public Prompt acceptInput(ConversationContext arg0, String arg1) {
        if(arg1.equals("done")) {
            return parent;
        } else if (arg1.length() > 0 && (arg1.charAt(0) == 'u' || arg1.charAt(0) == 'd')) {
            String[] split = arg1.split(" ", 2);
            if (split.length == 2) {
                int value = 0;
                try {
                    value = Integer.parseInt(split[1]);
                    if(value < 0)
                        value = 0;
                } catch (NumberFormatException e) {
                    return this;
                }

                if(arg1.charAt(0) == 'u')
                    value = -value;

                addToCurrentIndex(value);
            }
        }

        return this;
    }

    protected void addToCurrentIndex(int value) {
        if(value < 0) {
            currentIndex = Math.max(0, currentIndex + value);
        }else{
            currentIndex = Math.min(Math.max(0, currentData.size() - CONTENTSLINE_PER_PAGE), currentIndex + value);
        }
    }

    protected T get(int index) {
        return currentData.get(index);
    }

    protected void add(T value) {
        currentData.add(value);
    }

    protected void set(int index, T value) {
        if(!validateIndex(index))
            return;

        currentData.set(index, value);
    }

    protected void delete(int index) {
        if(!validateIndex(index))
            return;

        currentData.remove(index);
    }

    protected void swap(int index1, int index2) {
        if(!validateIndex(index1) || !validateIndex(index2))
            return;

        T temp = currentData.get(index1);
        currentData.set(index1, currentData.get(index2));
        currentData.set(index2, temp);
    }

    private boolean validateIndex(int index) {
        return !currentData.isEmpty() && index >= 0 && index < currentData.size();
    }

    @Override
    protected void print(Conversable conv) {
        super.print(conv);

        revalidateIndex();

        for (int i = 0; i < CONTENTSLINE_PER_PAGE; i++) {
            int realIndex = currentIndex + i;
            if(realIndex >= currentData.size())
                continue;

            conv.sendRawMessage(realIndex + ". " + currentData.get(realIndex));
        }
        conv.sendRawMessage("");

        conv.sendRawMessage(currentIndex + "/"+ currentData.size());
        conv.sendRawMessage("");

        conv.sendRawMessage("d [num] - go down the list");
        conv.sendRawMessage("u [num] - go up the list");
        conv.sendRawMessage("done - finish editing");
        conv.sendRawMessage("");
    }

    private void revalidateIndex() {
        int validIndex = currentData.size() - 1 - CONTENTSLINE_PER_PAGE;
        if(currentIndex > validIndex)
            currentIndex = Math.max(0, validIndex);
    }
}
