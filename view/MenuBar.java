package view;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MenuBar extends JMenuBar {

	public interface EditMenuListener {
		void onGroupRequested();

		void onUngroupRequested();

		void onLabelRequested();
	}

	public MenuBar() {
		this(null);
	}

	public MenuBar(EditMenuListener listener) {
		JMenu fileMenu = new JMenu("File");
		add(fileMenu);

		// 新增 Menu Item
		JMenu editMenu = new JMenu("Edit");
		JMenuItem groupItem = new JMenuItem("Group");
		JMenuItem ungroupItem = new JMenuItem("Ungroup");
		JMenuItem labelItem = new JMenuItem("Label");

		// 為 Menu Item 添加 ActionListener
		if (listener != null) {
			groupItem.addActionListener(e -> listener.onGroupRequested());
			ungroupItem.addActionListener(e -> listener.onUngroupRequested());
			labelItem.addActionListener(e -> listener.onLabelRequested());
		}

		editMenu.add(groupItem);
		editMenu.add(ungroupItem);
		editMenu.add(labelItem);
		add(editMenu);
	}
}
