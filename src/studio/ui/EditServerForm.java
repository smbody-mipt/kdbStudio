package studio.ui;

import studio.kdb.Server;

import javax.swing.*;

public class EditServerForm extends ServerForm {
//@TODO Can we Server.NO_SERVER modified here??
    public EditServerForm(JFrame owner,Server server) {
        super(owner,"Edit Server Details",server);
    }
}
