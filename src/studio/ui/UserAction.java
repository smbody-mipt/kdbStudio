package studio.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class UserAction extends AbstractAction {
    public UserAction(String text,
                      Icon icon,
                      String desc,
                      Integer mnemonic,
                      KeyStroke key) {
        super(text,icon);
        putValue(SHORT_DESCRIPTION,desc);
        putValue(MNEMONIC_KEY,mnemonic);
        putValue(ACCELERATOR_KEY,key);
    }

    public String getText() {
        return (String)getValue(NAME);
    }

    public KeyStroke getKeyStroke() {
        return (KeyStroke)getValue(ACCELERATOR_KEY);
    }

    public static UserAction create(String text, Icon icon,
                               String desc, int mnemonic,
                               KeyStroke key, ActionListener listener) {
        return new UserAction(text, icon, desc, mnemonic, key) {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.actionPerformed(e);
            }
        };
    }

    public void setSelected(boolean value) {
        putValue(SELECTED_KEY, value);
    }

    public static UserAction create(String text,
                             String desc, int mnemonic,
                             KeyStroke key, ActionListener listener) {
        return create(text, null, desc, mnemonic, key, listener);
    }

    public static UserAction create(String text,
                             String desc, int mnemonic,
                             ActionListener listener) {
        return create(text, null, desc, mnemonic, null, listener);
    }

    public static UserAction create(String text, ActionListener listener) {
        return create(text, null, null, 0, null, listener);
    }
}
