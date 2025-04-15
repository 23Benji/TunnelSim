package tunnel.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class ClientForm extends JFrame {

    public static final int MAX_GROUP_SIZE = 50;

    private static final Color COLOR_BEIGE_BG = new Color(220, 215, 201);
    private static final Color COLOR_BROWN_ACCENT = new Color(162, 123, 92);
    private static final Color COLOR_GREEN_GREY_MED = new Color(63, 79, 68);
    private static final Color COLOR_GREEN_GREY_DARK = new Color(44, 57, 48);

    private static final Color COLOR_PANEL = COLOR_BEIGE_BG;
    private static final Color COLOR_TEXT = COLOR_GREEN_GREY_DARK;
    private static final Color COLOR_TEXT_LIGHT = COLOR_GREEN_GREY_MED;
    private static final Color COLOR_PRIMARY_BUTTON = COLOR_BROWN_ACCENT;
    private static final Color COLOR_PRIMARY_BUTTON_HOVER = new Color(142, 103, 72);
    private static final Color COLOR_PRIMARY_BUTTON_TEXT = COLOR_BEIGE_BG;
    private static final Color COLOR_SECONDARY_BUTTON = COLOR_GREEN_GREY_MED;
    private static final Color COLOR_SECONDARY_BUTTON_HOVER = COLOR_GREEN_GREY_DARK;
    private static final Color COLOR_SECONDARY_BUTTON_TEXT = COLOR_BEIGE_BG;
    private static final Color COLOR_LIST_SELECTION = new Color(195, 190, 176);
    private static final Color COLOR_BORDER = new Color(195, 190, 176);

    private static final Font FONT_GENERAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 14);

    // --- Component Fields ---
    private GuidesMonitor guidesMonitor;
    private DefaultListModel<String> mActiveVisits;
    private JList<String> activeVisitsList;
    private JLabel availableGuidesLabel;
    private JLabel visitorsLabel;
    private JTextField visitorsField;
    private JButton requestVisitButton;
    private JLabel activeVisitsLabel;
    private JButton finishVisitButton;
    private JLabel availableVisitorsLabel;
    private JLabel statusTitleLabel;
    private JTextArea statusTextArea;

    // --- Added Fields ---
    private final String entranceName;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");


    public ClientForm(String entranceName) {
        super(entranceName);
        this.entranceName = entranceName;

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.put("control", COLOR_PANEL);
                    UIManager.put("nimbusBase", COLOR_GREEN_GREY_MED);
                    UIManager.put("nimbusFocus", COLOR_PRIMARY_BUTTON.brighter());
                    UIManager.put("List.background", COLOR_PANEL);
                    UIManager.put("List.foreground", COLOR_TEXT);
                    UIManager.put("List.selectionBackground", COLOR_LIST_SELECTION);
                    UIManager.put("List.selectionForeground", COLOR_TEXT);
                    UIManager.put("TextArea.background", COLOR_PANEL);
                    UIManager.put("TextArea.foreground", COLOR_TEXT);
                    UIManager.put("TextArea.inactiveForeground", COLOR_TEXT_LIGHT);
                    UIManager.put("TextField.background", COLOR_PANEL);
                    UIManager.put("TextField.foreground", COLOR_TEXT);
                    UIManager.put("TextField.inactiveForeground", COLOR_TEXT_LIGHT);
                    UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                            BorderFactory.createEmptyBorder(5, 8, 5, 8)
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
        UpdateVisitors("?");
        startMonitoringAvailability();

        // Frame settings
        getContentPane().setBackground(COLOR_BEIGE_BG);
        setSize(700, 500);
        setMinimumSize(new Dimension(650, 450));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Code below overrides the centering and places it near top-left
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = screenSize.height;
        int windowHeight = getHeight(); // Must be called AFTER setSize
        int y = (screenHeight - windowHeight) / 2;
        setLocation(10, y); // Set final location
        setVisible(true);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(COLOR_BEIGE_BG);

        // ========== LEFT PANEL ==========
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(15, 15, 15, 15))
        );
        leftPanel.setBackground(COLOR_PANEL);
        leftPanel.setOpaque(true);

        // --- Components ---
        availableGuidesLabel = createStyledLabel("Available guides: ? / " + GuidesMonitor.MAX_GUIDES);
        leftPanel.add(availableGuidesLabel);
        leftPanel.add(Box.createVerticalStrut(10));

        visitorsLabel = createStyledLabel("Visitors:");
        leftPanel.add(visitorsLabel);

        visitorsField = new JTextField(10);
        visitorsField.setFont(FONT_GENERAL);
        visitorsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, visitorsField.getPreferredSize().height + 5));
        configureComponentAlignment(visitorsField);
        leftPanel.add(visitorsField);
        leftPanel.add(Box.createVerticalStrut(10));

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
        JScrollPane scrollPane = new JScrollPane(activeVisitsList);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        scrollPane.setMinimumSize(new Dimension(150, 100));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        configureComponentAlignment(scrollPane);
        leftPanel.add(scrollPane);
        leftPanel.add(Box.createVerticalStrut(10));

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
        rightPanel.setBackground(COLOR_BEIGE_BG);

        statusTitleLabel = createStyledLabel("Status Log", true);
        statusTitleLabel.setFont(FONT_TITLE);
        statusTitleLabel.setForeground(COLOR_TEXT);
        rightPanel.add(statusTitleLabel, BorderLayout.NORTH);

        statusTextArea = new JTextArea();
        statusTextArea.setFont(FONT_GENERAL);
        statusTextArea.setEditable(false);
        statusTextArea.setLineWrap(true);
        statusTextArea.setWrapStyleWord(true);
        statusTextArea.setMargin(new Insets(5, 8, 5, 8));
        JScrollPane statusScrollPane = new JScrollPane(statusTextArea);
        rightPanel.add(statusScrollPane, BorderLayout.CENTER);

        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // ========== ACTION LISTENERS ==========
        requestVisitButton.addActionListener(e -> RequestVisit());
        finishVisitButton.addActionListener(e -> EndVisit());

        // ToolTips
        visitorsField.setToolTipText("Enter number of visitors (1-" + MAX_GROUP_SIZE + ")");
        requestVisitButton.setToolTipText("Request entry for the specified number of visitors");
        finishVisitButton.setToolTipText("End the currently selected visit from the list above");
        activeVisitsList.setToolTipText("List of visits currently active from this entrance");

        getContentPane().add(mainPanel);
    }

    private JLabel createStyledLabel(String text) {
        return createStyledLabel(text, false);
    }

    private JLabel createStyledLabel(String text, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(bold ? FONT_BOLD : FONT_GENERAL);
        label.setForeground(COLOR_TEXT_LIGHT);
        configureLabelAlignment(label);
        return label;
    }

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

    private static class RoundedButton extends JButton {
        private Color backgroundColor;
        private Color hoverColor;
        private Color pressedColor;
        private Color textColor;
        private boolean hovering = false;
        private int cornerRadius = 15;

        public RoundedButton(String text, Color background, Color hover, Color fontColor) {
            super(text);
            this.backgroundColor = background;
            this.hoverColor = hover;
            this.pressedColor = hover.darker();
            this.textColor = fontColor;

            setFont(FONT_BOLD);
            setForeground(textColor);
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
    //          EVENT HANDLING, LOGIC, and GUI UPDATE METHODS
    // ========================================================================

    /**
     * Handles the action for requesting a new visit.
     */
    private void RequestVisit() {
        String rawInput = visitorsField.getText();
        String trimmedInput = (rawInput != null) ? rawInput.trim() : "";

        if (trimmedInput.isEmpty()) {
            displayWarning("Visitor count input is required.");
            return;
        }

        int participantCount;
        try {
            participantCount = Integer.parseInt(trimmedInput);

            if (participantCount <= 0) {
                displayError("The number of visitors must be positive.");
            } else if (participantCount > MAX_GROUP_SIZE) {
                displayWarning("A maximum of " + MAX_GROUP_SIZE + " visitors is allowed per group.");
            } else {
                new ClientThread(participantCount, this, guidesMonitor).start();
                visitorsField.setText("");
            }
        } catch (NumberFormatException formatException) {
            displayError("Invalid entry: Please use only numbers for visitor count.");

        }
        visitorsField.requestFocusInWindow();
    }

    /**
     * Handles the action for ending a selected visit.
     */
    private void EndVisit() {
        int selectedListPosition = activeVisitsList.getSelectedIndex();

        if (selectedListPosition < 0) {
            displayWarning("No visit selected from the list to end.");
        } else {
            String selectedItemText = mActiveVisits.getElementAt(selectedListPosition);
            try {
                String[] parts = selectedItemText.split(" ");
                if (parts.length < 1) {
                    throw new ArrayIndexOutOfBoundsException("Could not split entry text.");
                }
                String countStr = parts[0];
                int groupSizeToEnd = Integer.parseInt(countStr);

                if (groupSizeToEnd > 0) {
                    new ClientThread(-groupSizeToEnd, this, guidesMonitor).start();

                    final int indexToRemoveSafely = selectedListPosition;
                    if (SwingUtilities.isEventDispatchThread()) {
                        mActiveVisits.remove(indexToRemoveSafely);
                        activeVisitsList.clearSelection();
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            mActiveVisits.remove(indexToRemoveSafely);
                            activeVisitsList.clearSelection();
                        });
                    }
                } else {
                    displayError("Invalid visitor count found in list entry: '" + selectedItemText + "'");
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException processingError) {
                displayError("Error processing selected list item: '" + selectedItemText + "'");
            }
        }
    }

    private static final long POLLING_DELAY_MS = 1500L;
    private static final long POLLING_INTERVAL_MS = 1000L;
    private Timer recurringCapacityCheck;

    private void startMonitoringAvailability() {
        this.stopPeriodicChecks();

        TimerTask capacityPollingAction = new TimerTask() {
            @Override
            public void run() {
                new ClientThread(0, ClientForm.this, guidesMonitor).start();
            }
        };

        this.recurringCapacityCheck = new Timer("Poller-" + entranceName + "-Thread", true);

        this.recurringCapacityCheck.scheduleAtFixedRate(
                capacityPollingAction,
                POLLING_DELAY_MS,
                POLLING_INTERVAL_MS
        );
    }

    /**
     * Helper to stop the periodic check timer
     */
    private void stopPeriodicChecks() {
        if (this.recurringCapacityCheck != null) {
            this.recurringCapacityCheck.cancel();
            this.recurringCapacityCheck = null;
        }
    }

    /**
     * Appends a message to the status area, ensuring EDT execution.
     */
    public void refreshStatus(String statusUpdate) {
        String timestamp = LocalTime.now().format(TIME_FORMATTER);
        String logEntry = "[" + timestamp + "] " + statusUpdate; // Add timestamp prefix

        if (SwingUtilities.isEventDispatchThread()) {
            statusTextArea.append(logEntry + "\n");
            statusTextArea.setCaretPosition(statusTextArea.getDocument().getLength());
        } else {
            SwingUtilities.invokeLater(() -> {
                statusTextArea.append(logEntry + "\n");
                statusTextArea.setCaretPosition(statusTextArea.getDocument().getLength());
            });
        }
    }


    /**
     * Updates the guides label, ensuring EDT execution.
     */
    public void updateAvailableGuides(int currentGuides) {
        final String guideStatusText = "Available guides: " + currentGuides + " / " + GuidesMonitor.MAX_GUIDES;
        if (SwingUtilities.isEventDispatchThread()) {
            if (availableGuidesLabel != null) {
                availableGuidesLabel.setText(guideStatusText);
            }
        } else {
            SwingUtilities.invokeLater(() -> updateAvailableGuides(currentGuides));
        }
    }

    /**
     * Updates the available visitors label, ensuring EDT execution.
     */
    public void UpdateVisitors(String visitorsValue) {
        final String visitorInfoText = "Available visitors: " + visitorsValue;
        if (SwingUtilities.isEventDispatchThread()) {
            if (availableVisitorsLabel != null) {
                availableVisitorsLabel.setText(visitorInfoText);
            }
        } else {
            SwingUtilities.invokeLater(() -> UpdateVisitors(visitorsValue));
        }
    }

    /**
     * Adds an entry to the active visits list, ensuring EDT execution.
     */
    public void displayNewVisit(int numberOfParticipants) {
        final String visitInfo = numberOfParticipants + " visitors";
        if (SwingUtilities.isEventDispatchThread()) {
            if (mActiveVisits != null) {
                mActiveVisits.addElement(visitInfo);
            }
        } else {
            SwingUtilities.invokeLater(() -> displayNewVisit(numberOfParticipants));
        }
    }

    // --- Message Dialogs ---

    /**
     * Displays an error message in the status area and as a popup dialog.
     */
    private void displayError(String problemDescription) {
        refreshStatus("ERROR: " + problemDescription);
        JOptionPane.showMessageDialog(this,
                problemDescription,
                "Error - " + entranceName,
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays a warning message in the status area and as a popup dialog.
     */
    private void displayWarning(String warningDetails) {
        refreshStatus("Warning: " + warningDetails);
        JOptionPane.showMessageDialog(this,
                warningDetails,
                "Warning - " + entranceName,
                JOptionPane.WARNING_MESSAGE);
    }

    // --- Main Method ---
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