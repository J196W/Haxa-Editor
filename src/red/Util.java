package red;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public final class Util {
	private Util() throws InstantiationException {
		throw new InstantiationException();
	}
	
	public enum FileType {
		FILE,
		DIR
	}
	
	public static final int COLOR_BYTE_LENGTH = 3; // Each color takes up 1 byte;
	public static final String OLD = File.separator + "backup";
	public static final Color BACKGROUND = new Color(245, 245, 245);
	public static final String SAVE_QUESTION_TITLE = "Really save?";
	public static final String SAVE_QUESTION_MESSAGE = "Do you really want to save this information?\n"
													 + "(The current file will be backed up)";
	public static final String SAVE_SUCCESS_TITLE = "Succcess!";
	public static final String SAVE_SUCCESS_MESSAGE = "Wrote file successfully!";
	public static final String SAVE_FAIL_TITLE = "Fail!";
	public static final String SAVE_FAIL_MESSAGE = "Error writing the level!\n"
												 + "Check the console for details.";
	
	

	/**
	 * Gets a directory. If it does not get the directory, it will create it
	 * If it cannot edit the directory, then the program will quit.
	 * @param folder The directory to read.
	 * @return The proper folder.
	 */
	public static File getDir(File folder) {
		if(!folder.exists()) {
			folder.mkdir();
			System.out.println("Created directory: '" + folder.getAbsolutePath() + "'");
		} else if(!folder.isDirectory()) {
			System.out.println("Folder '" + folder.getAbsolutePath() + "' is not a directory!");
			System.exit(0);
		} else if(!folder.canWrite()) {
			System.out.println("Cannot write to '" + folder.getAbsolutePath() + "'!");
			System.exit(0);
		} else {
			System.out.println("Opened directory: '" + folder.getAbsolutePath() + "'");
		}
		return folder;
	}
	

	/**
	 * Gets the contents of a directory depending on the passed FileType
	 * @param type Pass either FILE to get files, or DIR to get dirs.
	 * @param dir The directory to search in.
	 * @param foundMessage The message to display in the console when files/dirs are found.
	 * @param notFoundMessage The message to display in the console when files are not found.
	 * @return An array list of either files or directories
	 */
	public static ArrayList<File> getDirContents(FileType type, File dir, String foundMessage, String notFoundMessage) {
		boolean foundPath = false;
		ArrayList<File> paths = new ArrayList<>();
		for(String pathName : dir.list()) {
			File path = new File(dir, pathName);
			if((type == FileType.DIR ? path.isDirectory() : path.isFile())) {
				if(!foundPath) {
					foundPath = true;
					System.out.println(foundMessage + ":");
				}
				paths.add(path);
				System.out.println(pathName);
			}
		}
		if(!foundPath) System.out.println(notFoundMessage + "!");
		System.out.println();
		return paths;
	}
	
	/**
	 * Checks to make sure that a string in the file appears
	 * (Useful for checking the header or footer of the file)
	 * @param data Raw data to check
	 * @param s The string to compare (will read as many bytes as passed string)
	 * @throws IOException
	 * @throws BufferUnderflowException
	 */
	public static void checkString(ByteBuffer data, String s) throws IOException, BufferUnderflowException {
		for(char header : s.toCharArray()) {
			byte file = data.get();
			if(header != file) {
				throw new IOException("Invalid string!");
			}
		}
	}
	
	/**
	 * Reads a sized, dynamic string from the file.
	 * @param data Raw data
	 * @return A string.
	 * @throws IOException
	 * @throws BufferUnderflowException
	 */
	public static String readString(ByteBuffer data) throws IOException, BufferUnderflowException {
		byte length = data.get();
		StringBuilder string = new StringBuilder();
		for(int i = 0; i < length; i++) {
			string.append((char)data.get());
		}
		return string.toString();
	}

	/**
	 * Reads a list of colors from the file.
	 * @param data Raw data.
	 * @return An array list of read colors.
	 * @throws IOException
	 * @throws BufferUnderflowException
	 */
	public static ArrayList<Color> readColors(ByteBuffer data) throws IOException, BufferUnderflowException {
		int length = data.get() & 0xFF;
		ArrayList<Color> colors = new ArrayList<>();
		for(int i = 0; i < length; i++) {
			colors.add(new Color(data.get() & 0xFF, data.get() & 0xFF, data.get() & 0xFF));
		}
		return colors;
	}

	/**
	 * Opens a binary file for reading.
	 * @param file The file  to open
	 * @return A  binary ByteBuffer representing the whole file.
	 * @throws IOException
	 */
	public static ByteBuffer readBinaryFile(File file) throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		FileChannel channel = inputStream.getChannel();
		ByteBuffer rawData = ByteBuffer.allocate((int)channel.size());
		channel.read(rawData);
		rawData.rewind();
		rawData.order(ByteOrder.LITTLE_ENDIAN);
		inputStream.close();
		return rawData;
	}
	
	/**
	 * Creates and colors a new panel.
	 * @param manager The layout of the panel to create.
	 * @return A new panel.
	 */
	public static JPanel startFrame(LayoutManager manager) {
		JPanel frame = new JPanel();
		frame.setBackground(BACKGROUND);
		frame.setLayout(manager);
		return frame;
	}

	/**
	 * Adds a text box to a panel surrounded with a border  that has words.
	 * @param panel The panel to attach the field to.
	 * @param constraints Constraints for the panel.
	 * @param title The title of the panel.
	 * @param defaultText The default text to insert into the panel.
	 * @return The text field  added, with an action listener to auto select text when clicked in.
	 */
	public static JTextField addTitledFieldToPanel(JPanel panel, Object constraints, String title, String defaultText) {
		JTextField text = new JTextField(defaultText);
		text.setBorder(BorderFactory.createTitledBorder(title));
		text.setBackground(BACKGROUND);
		panel.add(text, constraints);
		
		text.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				text.selectAll();
			}
			public void focusLost(FocusEvent e) {}
		});
		
		return text;
	}
	
	/**
	 * Adds a titled list to a panel.
	 * @param panel The panel to add the list to.
	 * @param constraints The constraints of the list.
	 * @param title The title to put around the list
	 * @param list The list of data to add to the panel (must have a toString method)!
	 * @return The added list and model.
	 */
	public static ListData addTitledListToPanel(JPanel panel, Object constraints, String title, ArrayList<?> list) {
		DefaultListModel<String> model = new DefaultListModel<>();
		for(Object o : list) {
			model.addElement(o.toString());
		}
		JList<String> internal = new JList<>(model);
		JScrollPane scroller = new JScrollPane(internal);
		scroller.setBorder(BorderFactory.createEmptyBorder());
		internal.setBorder(BorderFactory.createTitledBorder(title));
		internal.setBackground(BACKGROUND);
		panel.add(scroller, constraints);
		return new ListData(model, internal);
	}
	
	/**
	 * Adds a button to a panel.
	 * @param panel The panel to  add the button to.
	 * @param constraints The constraints of the button.
	 * @param text The button text.
	 * @return The created button.
	 */
	public static JButton addButtonToPanel(JPanel panel, Object constraints, String text) {
		JButton button = new JButton(text);
		button.setBackground(BACKGROUND);
		panel.add(button, constraints);
		return button;
	}
	
	public static void createColorPicker(JPanel colors, ArrayList<Color> colorList, String name) {
		JPanel sub = startFrame(new BorderLayout());
		ListData data = addTitledListToPanel(sub, BorderLayout.CENTER, name, colorList);
		JPanel buttons = startFrame(new GridLayout(1, 0));
		JButton jADDN = addButtonToPanel(buttons, null, "Add");
		JButton jREMO = addButtonToPanel(buttons, null, "Remove");
		JButton jEDIT = addButtonToPanel(buttons, null, "Edit");
		sub.add(buttons, BorderLayout.SOUTH);
		colors.add(sub);
		
		updateColorList(data, colorList);
		
		jADDN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(null, "Add new color for " + name, Color.WHITE);
				if(newColor == null) return;
				newColor = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), 255);
				colorList.add(newColor);
				updateColorList(data, colorList);
			}
		});
		jREMO.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int oldColor = data.list.getSelectedIndex();
				if(oldColor < 0) return;
				colorList.remove(oldColor);
				updateColorList(data, colorList);
			}
		});
		jEDIT.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int oldColor = data.list.getSelectedIndex();
				if(oldColor < 0) return;
				Color newColor = JColorChooser.showDialog(null, "Edit color for " + name, colorList.get(oldColor));
				if(newColor == null) return;
				newColor = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), 255);
				colorList.set(oldColor, newColor);
				updateColorList(data, colorList);
			}
		});
	}

	public static void updateColorList(ListData data, ArrayList<Color> colorList) {
		int selected = data.list.getSelectedIndex();
		int direction = colorList.size() - data.model.size();
		data.model.clear();
		for(Color c : colorList) data.model.addElement(c.toString().substring(14));
		fixSelectedIndex(data, colorList, selected, direction);
	}

	public static void updateList(ListData data, ArrayList<?> list) {
		int selected = data.list.getSelectedIndex();
		int direction = list.size() - data.model.size();
		data.model.clear();
		for(Object o : list) data.model.addElement(o.toString());
		fixSelectedIndex(data, list, selected, direction);
	}

	public static String upperText(JTextField text) {
		String str = text.getText().toUpperCase();
		text.setText(str);
		return str;
	}
	
	public static String getText(JTextField text) {
		return text.getText();
	}

	private static void fixSelectedIndex(ListData data, ArrayList<?> list, int selected, int difference) {
		if(selected + difference < list.size()) {
			data.list.setSelectedIndex((selected + difference >= 0 ? selected + difference : 0));
		}
	}
}
