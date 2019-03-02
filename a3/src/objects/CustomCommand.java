package objects;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;

public class CustomCommand extends AbstractAction{
	private Object target;
	
	public CustomCommand(String text, String description, String mnemonic) {
		super(text);
		putValue(SHORT_DESCRIPTION, description);
		putValue(MNEMONIC_KEY, mnemonic);
	}
	public CustomCommand(String text, String description, Object target) {
		super(text);
		putValue(SHORT_DESCRIPTION, description);
		setTarget(target);
	}
	
	public void setTarget(Object target) {
		this.target = target; 
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		System.out.format("%s pressed!%n", this.getValue(NAME));
		((a3.Starter)target).CommandUpdate(this.getValue(NAME).toString());
	}

	
}
