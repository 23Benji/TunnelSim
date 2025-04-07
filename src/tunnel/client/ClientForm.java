package tunnel.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Timer;
import java.util.TimerTask;

public class ClientForm extends JFrame {

	// --- Constants ---
	public static final int MAX_GROUP_SIZE = 50;

	// --- New Earthy/Muted Styling Constants ---
	// Core Palette Provided
	private static final Color COLOR_BEIGE_BG       = new Color(220, 215, 201); // Main background, button text
	private static final Color COLOR_BROWN_ACCENT   = new Color(162, 123, 92); // Primary button
	private static final Color COLOR_GREEN_GREY_MED = new Color(63, 79, 68);  // Secondary button, light text
	private static final Color COLOR_GREEN_GREY_DARK= new Color(44, 57, 48);  // Main text, secondary hover

	// Derived Colors
	private static final Color COLOR_PANEL          = COLOR_BEIGE_BG; // Keep panel same as background for flat look
	private static final Color COLOR_TEXT           = COLOR_GREEN_GREY_DARK; // Darkest color for text
	private static final Color COLOR_TEXT_LIGHT     = COLOR_GREEN_GREY_MED; // Lighter text for less emphasis
	private static final Color COLOR_PRIMARY_BUTTON = COLOR_BROWN_ACCENT;
	private static final Color COLOR_PRIMARY_BUTTON_HOVER = new Color(142, 103, 72); // Darker brown
	private static final Color COLOR_PRIMARY_BUTTON_TEXT = COLOR_BEIGE_BG; // Use background beige for text
	private static final Color COLOR_SECONDARY_BUTTON = COLOR_GREEN_GREY_MED;
	private static final Color COLOR_SECONDARY_BUTTON_HOVER = COLOR_GREEN_GREY_DARK; // Use darkest green-grey for hover
	private static final Color COLOR_SECONDARY_BUTTON_TEXT = COLOR_BEIGE_BG; // Use background beige for text
	private static final Color COLOR_LIST_SELECTION = new Color(195, 190, 176); // Subtly darker/greyer beige for selection
	private static final Color COLOR_BORDER         = new Color(195, 190, 176); // Slightly darker beige for borders

	// Fonts (Using Segoe UI - replace if needed)
	private static final Font FONT_GENERAL = new Font("Segoe UI", Font.PLAIN, 13);
	private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
	private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 14);

	// --- Component Fields (Your Original Names) ---
	private GuidesMonitor guidesMonitor;
	private DefaultListModel<String> mActiveVisits;
	private JList<String> activeVisitsList;
	private JLabel availableGuidesLabel;
	private JLabel visitorsLabel;
	private JTextField visitorsField;
	private JButton requestVisitButton; // Will be RoundedButton
	private JLabel activeVisitsLabel;
	private JButton finishVisitButton;  // Will be RoundedButton
	private JLabel availableVisitorsLabel;
	private JLabel statusTitleLabel;
	private JTextArea statusTextArea;

	// --- Added Fields ---
	private final String entranceName;
	private Timer availabilityCheckTimer;

	public ClientForm(String entranceName) {
		super(entranceName);
		this.entranceName = entranceName;

		// Apply Nimbus Look and Feel early
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					// Customize Nimbus defaults with the new palette
					UIManager.put("control", COLOR_PANEL); // General control background
					UIManager.put("nimbusBase", COLOR_GREEN_GREY_MED); // Use medium green-grey as base
					UIManager.put("nimbusFocus", COLOR_PRIMARY_BUTTON.brighter()); // Brighter brown for focus ring
					UIManager.put("List.background", COLOR_PANEL);
					UIManager.put("List.foreground", COLOR_TEXT);
					UIManager.put("List.selectionBackground", COLOR_LIST_SELECTION);
					UIManager.put("List.selectionForeground", COLOR_TEXT); // Keep text dark on selection
					UIManager.put("TextArea.background", COLOR_PANEL);
					UIManager.put("TextArea.foreground", COLOR_TEXT);
					UIManager.put("TextArea.inactiveForeground", COLOR_TEXT_LIGHT); // Slightly lighter when inactive
					UIManager.put("TextField.background", COLOR_PANEL);
					UIManager.put("TextField.foreground", COLOR_TEXT);
					UIManager.put("TextField.inactiveForeground", COLOR_TEXT_LIGHT);
					// Use a compound border: rounded line + padding
					UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
							BorderFactory.createLineBorder(COLOR_BORDER, 1, true), // Rounded LineBorder
							BorderFactory.createEmptyBorder(5, 8, 5, 8) // Padding
					));
					UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(COLOR_BORDER));
					break;
				}
			}
		} catch (Exception e) {
			System.err.println("Nimbus LaF not available, using default.");
		}

		initUI();
		guidesMonitor = new GuidesMonitor(this);
		updateAvailableGuides(guidesMonitor.getAvailableGuides());
		setAvailableVisitors("?");
		startAvailabilityChecker();

		// Frame settings
		getContentPane().setBackground(COLOR_BEIGE_BG); // Use new background
		setSize(700, 500);
		setMinimumSize(new Dimension(650, 450));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenHeight = screenSize.height;
		int windowHeight = getHeight();
		int y = (screenHeight - windowHeight) / 2;
		setLocation(10, y);
		setVisible(true);
	}

	private void initUI() {
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.setBackground(COLOR_BEIGE_BG); // Use new background

		// ========== LEFT PANEL ==========
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(COLOR_BORDER), // Use new border color
				BorderFactory.createEmptyBorder(15, 15, 15, 15))
		);
		leftPanel.setBackground(COLOR_PANEL); // Use new panel color
		leftPanel.setOpaque(true);

		// --- Components ---
		availableGuidesLabel = createStyledLabel("Available guides: ? / " + GuidesMonitor.MAX_GUIDES);
		leftPanel.add(availableGuidesLabel);
		leftPanel.add(Box.createVerticalStrut(10));

		visitorsLabel = createStyledLabel("Visitors:");
		leftPanel.add(visitorsLabel);

		visitorsField = new JTextField(10);
		visitorsField.setFont(FONT_GENERAL);
		// Nimbus handles colors/border based on UIManager settings
		visitorsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, visitorsField.getPreferredSize().height + 5));
		configureComponentAlignment(visitorsField);
		leftPanel.add(visitorsField);
		leftPanel.add(Box.createVerticalStrut(10));

		// Use RoundedButton with new colors
		requestVisitButton = new RoundedButton("Request visit", COLOR_PRIMARY_BUTTON, COLOR_PRIMARY_BUTTON_HOVER, COLOR_PRIMARY_BUTTON_TEXT);
		configureComponentAlignment(requestVisitButton);
		leftPanel.add(requestVisitButton);
		leftPanel.add(Box.createVerticalStrut(20));

		activeVisitsLabel = createStyledLabel("Active visits:", true);
		leftPanel.add(activeVisitsLabel);
		leftPanel.add(Box.createVerticalStrut(5));

		mActiveVisits = new DefaultListModel<>();
		activeVisitsList = new JList<>(mActiveVisits);
		activeVisitsList.setFont(FONT_GENERAL);
		activeVisitsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Nimbus handles colors based on UIManager settings
		JScrollPane scrollPane = new JScrollPane(activeVisitsList);
		scrollPane.setPreferredSize(new Dimension(200, 150));
		scrollPane.setMinimumSize(new Dimension(150, 100));
		scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
		// Nimbus handles border based on UIManager settings
		configureComponentAlignment(scrollPane);
		leftPanel.add(scrollPane);
		leftPanel.add(Box.createVerticalStrut(10));

		// Use RoundedButton with new colors
		finishVisitButton = new RoundedButton("Finish visit", COLOR_SECONDARY_BUTTON, COLOR_SECONDARY_BUTTON_HOVER, COLOR_SECONDARY_BUTTON_TEXT);
		configureComponentAlignment(finishVisitButton);
		leftPanel.add(finishVisitButton);
		leftPanel.add(Box.createVerticalStrut(20));

		availableVisitorsLabel = createStyledLabel("Available visitors: ?");
		leftPanel.add(availableVisitorsLabel);

		leftPanel.add(Box.createVerticalGlue());

		mainPanel.add(leftPanel, BorderLayout.WEST);

		// ========== RIGHT PANEL ==========
		JPanel rightPanel = new JPanel(new BorderLayout(0, 5));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		rightPanel.setBackground(COLOR_BEIGE_BG); // Use new background

		statusTitleLabel = createStyledLabel("Status Log", true);
		statusTitleLabel.setFont(FONT_TITLE);
		statusTitleLabel.setForeground(COLOR_TEXT); // Use darker text for title
		rightPanel.add(statusTitleLabel, BorderLayout.NORTH);

		statusTextArea = new JTextArea();
		statusTextArea.setFont(FONT_GENERAL);
		statusTextArea.setEditable(false);
		statusTextArea.setLineWrap(true);
		statusTextArea.setWrapStyleWord(true);
		statusTextArea.setMargin(new Insets(5, 8, 5, 8));
		// Nimbus handles colors/border based on UIManager settings
		JScrollPane statusScrollPane = new JScrollPane(statusTextArea);
		rightPanel.add(statusScrollPane, BorderLayout.CENTER);

		mainPanel.add(rightPanel, BorderLayout.CENTER);

		// ========== ACTION LISTENERS ==========
		requestVisitButton.addActionListener(e -> handleRequestVisit());
		finishVisitButton.addActionListener(e -> handleEndVisit());

		getContentPane().add(mainPanel);
	}

	// Helper to create styled labels (Uses new color constants)
	private JLabel createStyledLabel(String text) {
		return createStyledLabel(text, false);
	}

	private JLabel createStyledLabel(String text, boolean bold) {
		JLabel label = new JLabel(text);
		label.setFont(bold ? FONT_BOLD : FONT_GENERAL);
		label.setForeground(COLOR_TEXT_LIGHT); // Use the lighter text color
		configureLabelAlignment(label);
		return label;
	}

	// Helper to configure alignment for BoxLayout
	private void configureLabelAlignment(JLabel label) {
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
	}
	private void configureComponentAlignment(JComponent component) {
		component.setAlignmentX(Component.LEFT_ALIGNMENT);
		if (component instanceof JButton || component instanceof JTextField || component instanceof JScrollPane) {
			component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
		}
		if (component instanceof JScrollPane) {
			component.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		}
	}

	// --- Inner Class for Rounded Buttons (Unchanged logic, uses color constants) ---
	private static class RoundedButton extends JButton {
		private Color backgroundColor;
		private Color hoverColor;
		private Color pressedColor;
		private Color textColor;
		private boolean hovering = false;
		private int cornerRadius = 15;

		public RoundedButton(String text, Color background, Color hover, Color fontColor) {
			super(text);
			// These colors are now set from the new constants passed in
			this.backgroundColor = background;
			this.hoverColor = hover;
			this.pressedColor = hover.darker(); // Keep pressed slightly darker than hover
			this.textColor = fontColor;

			setFont(FONT_BOLD);
			setForeground(textColor); // Uses the passed-in text color constant
			setContentAreaFilled(false);
			setFocusPainted(false);
			setBorderPainted(false);
			setOpaque(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			setBorder(new EmptyBorder(8, 18, 8, 18));

			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					hovering = true;
					repaint();
				}
				@Override
				public void mouseExited(MouseEvent e) {
					hovering = false;
					repaint();
				}
			});
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Color bg;
			ButtonModel model = getModel();
			if (model.isPressed()) {
				bg = pressedColor;
			} else if (hovering) {
				bg = hoverColor;
			} else {
				bg = backgroundColor;
			}
			g2.setColor(bg);
			g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
			super.paintComponent(g2);
			g2.dispose();
		}
	}

	// ========================================================================
	//          EVENT HANDLING, LOGIC, and GUI UPDATE METHODS (Unchanged)
	// ========================================================================

	// --- Event Handlers ---
	private void handleRequestVisit() {
		int count;
		try {
			String countText = visitorsField.getText().trim();
			if (countText.isEmpty()) {
				showWarning("Please enter the number of visitors."); return;
			}
			count = Integer.parseInt(countText);
			if (count <= 0) {
				showError("Number of visitors must be positive!"); return;
			}
			if (count > MAX_GROUP_SIZE) {
				showWarning("Maximum " + MAX_GROUP_SIZE + " visitors per group allowed!"); return;
			}
			new ClientThread(count, this, guidesMonitor).start();
			updateStatus("Requesting visit for " + count + " visitors...");
			visitorsField.setText("");
		} catch (NumberFormatException ex) {
			showError("Invalid input: Only numbers are allowed!");
		}
	}

	private void handleEndVisit() {
		int selectedIndex = activeVisitsList.getSelectedIndex();
		if (selectedIndex == -1) {
			showWarning("No active visit selected to finish!"); return;
		}
		String selectedEntry = mActiveVisits.getElementAt(selectedIndex);
		try {
			String numberPart = selectedEntry.split(" ")[0];
			int count = Integer.parseInt(numberPart);
			if (count > 0) {
				new ClientThread(-count, this, guidesMonitor).start();
				updateStatus("Finishing visit for " + count + " visitors...");
				final int indexToRemove = selectedIndex;
				if (SwingUtilities.isEventDispatchThread()) {
					mActiveVisits.remove(indexToRemove);
					activeVisitsList.clearSelection();
				} else {
					SwingUtilities.invokeLater(() -> {
						mActiveVisits.remove(indexToRemove);
						activeVisitsList.clearSelection();
					});
				}
			} else {
				showError("Error reading visitor count from list (invalid number): " + selectedEntry);
			}
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
			showError("Internal error processing list entry: " + selectedEntry);
		}
	}

	// --- Timer ---
	private void startAvailabilityChecker() {
		if (availabilityCheckTimer != null) { availabilityCheckTimer.cancel(); }
		availabilityCheckTimer = new Timer("AvailabilityChecker-" + entranceName, true);
		availabilityCheckTimer.scheduleAtFixedRate(new TimerTask() {
			@Override public void run() {
				new ClientThread(0, ClientForm.this, guidesMonitor).start();
			}
		}, 1500, 1000);
	}

	// --- GUI Update Methods ---
	public void updateStatus(String message) {
		if (SwingUtilities.isEventDispatchThread()) {
			if(message.startsWith("Err:")) {
				message.replaceFirst("Err:", "Error: ");
			}
			statusTextArea.append(message + "\n");
			statusTextArea.setCaretPosition(statusTextArea.getDocument().getLength());
		} else { SwingUtilities.invokeLater(() -> updateStatus(message)); }
	}

	public void logError(String errorMessage) {
		if (SwingUtilities.isEventDispatchThread()) {
			statusTextArea.append("ERROR: " + errorMessage + "\n");
			statusTextArea.setCaretPosition(statusTextArea.getDocument().getLength());
		} else { SwingUtilities.invokeLater(() -> logError(errorMessage)); }
	}

	public void updateAvailableGuides(int available) {
		final String text = "Available guides: " + available + " / " + GuidesMonitor.MAX_GUIDES;
		if (SwingUtilities.isEventDispatchThread()) {
			if (availableGuidesLabel != null) { availableGuidesLabel.setText(text); }
		} else { SwingUtilities.invokeLater(() -> updateAvailableGuides(available)); }
	}

	public void updateAvailableCapacity(int available) {
		setAvailableVisitors(String.valueOf(available));
	}

	private void setAvailableVisitors(String text) {
		if (SwingUtilities.isEventDispatchThread()) {
			if (availableVisitorsLabel != null) { availableVisitorsLabel.setText("Available visitors: " + text); }
		} else { SwingUtilities.invokeLater(() -> setAvailableVisitors(text)); }
	}

	public void addActiveVisitEntry(int count) {
		final String entry = count + " visitors";
		if (SwingUtilities.isEventDispatchThread()) {
			if (mActiveVisits != null) { mActiveVisits.addElement(entry); }
		} else { SwingUtilities.invokeLater(() -> addActiveVisitEntry(count)); }
	}

	// --- Message Dialogs ---
	private void showError(String message) {
		logError(message);
		JOptionPane.showMessageDialog(this, message, "Error - " + entranceName, JOptionPane.ERROR_MESSAGE);
	}

	private void showWarning(String message) {
		updateStatus(message);
		JOptionPane.showMessageDialog(this, message, "Warning - " + entranceName, JOptionPane.WARNING_MESSAGE);
	}

	// --- Main Method ---
	/**
	 * Starts the Client application, creating two separate entrance windows
	 * positioned side-by-side.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			ClientForm entrance1 = new ClientForm("Entrance 1");

			Point location1 = entrance1.getLocation();
			int width1 = entrance1.getWidth();
			int gap = 10;

			ClientForm entrance2 = new ClientForm("Entrance 2");

			int newX = location1.x + width1 + gap;
			int newY = location1.y;

			entrance2.setLocation(newX, newY);
		});
	}
}