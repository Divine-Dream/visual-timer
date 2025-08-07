package com.visualtimer;

import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import net.runelite.client.chat.ChatMessageManager;

public class VisualTimerPanel extends PluginPanel
{
    private final VisualTimerOverlayManager overlayManager;
    private final VisualTimerPlugin plugin;
    private final JLabel previewLabel = new JLabel("0:00:00.0", SwingConstants.CENTER);
    private final JTextField nameField = new JTextField("Timer");
    private final JTextField hourField = new JTextField("0");
    private final JTextField minField = new JTextField("0");
    private final JTextField secField = new JTextField("0");
    private final JPanel timerListPanel = new JPanel();

    private final Color backgroundColor = new Color(38, 38, 38);
    private final Color inputBackground = new Color(30, 30, 30);
    private final ChatMessageManager chatMessageManager;

    @Inject
    public VisualTimerPanel(VisualTimerOverlayManager overlayManager, VisualTimerPlugin plugin, ChatMessageManager chatMessageManager)
    {
        this.overlayManager = overlayManager;
        this.plugin = plugin;
        this.chatMessageManager = chatMessageManager;

        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(backgroundColor);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createInputPanel(), BorderLayout.CENTER);
        add(createTimerListPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundColor);

        previewLabel.setFont(new Font("Arial", Font.BOLD, 32));
        previewLabel.setForeground(Color.WHITE);
        panel.add(previewLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInputPanel()
    {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(backgroundColor);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        styleTextField(nameField);
        nameField.setPreferredSize(new Dimension(200, 28));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JPanel labelRow = new JPanel(new GridLayout(1, 3, 10, 0));
        labelRow.setBackground(backgroundColor);
        labelRow.add(makeCenteredLabel("H:"));
        labelRow.add(makeCenteredLabel("M:"));
        labelRow.add(makeCenteredLabel("S:"));

        JPanel inputRow = new JPanel(new GridLayout(1, 3, 10, 0));
        inputRow.setBackground(backgroundColor);
        styleTextField(hourField);
        styleTextField(minField);
        styleTextField(secField);
        inputRow.add(hourField);
        inputRow.add(minField);
        inputRow.add(secField);

        JButton addButton = new JButton("Add Timer");
        styleButton(addButton, "#397A66");
        addButton.addActionListener(this::addTimer);
        addButton.setBorderPainted(false);

        JButton clearButton = new JButton("Remove All");
        styleButton(clearButton, "#C21614");
        clearButton.addActionListener(e -> {
            overlayManager.removeAllTimers();
            refreshTimerList();
        });
        clearButton.setBorderPainted(false);

        inputPanel.add(nameLabel);
        inputPanel.add(Box.createVerticalStrut(5));
        inputPanel.add(nameField);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(labelRow);
        inputPanel.add(inputRow);
        inputPanel.add(Box.createVerticalStrut(20));
        inputPanel.add(addButton);
        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(clearButton);

        return inputPanel;
    }

    private JPanel createTimerListPanel()
    {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setBackground(backgroundColor);

        JLabel label = new JLabel("Active Timers", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.WHITE);

        timerListPanel.setLayout(new BoxLayout(timerListPanel, BoxLayout.Y_AXIS));
        timerListPanel.setBackground(backgroundColor);

        JScrollPane scrollPane = new JScrollPane(timerListPanel);
        scrollPane.setPreferredSize(new Dimension(200, 250));
        scrollPane.setBackground(backgroundColor);
        scrollPane.getViewport().setBackground(backgroundColor);
        scrollPane.setBorder(null);

        JPanel labelWrapper = new JPanel();
        labelWrapper.setLayout(new BoxLayout(labelWrapper, BoxLayout.Y_AXIS));
        labelWrapper.setBackground(backgroundColor);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelWrapper.add(label);

        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        wrapper.add(labelWrapper, BorderLayout.NORTH);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return wrapper;
    }

    private void addTimer(ActionEvent e)
    {
        String name = nameField.getText().trim();
        if (name.isEmpty())
        {
            name = "Timer"; // Default name if none entered
            nameField.setText(name);
        }

        int hours = parseTime(hourField.getText());
        int minutes = parseTime(minField.getText());
        int seconds = parseTime(secField.getText());

        if (minutes > 59 || seconds > 59)
        {
            JOptionPane.showMessageDialog(
                    null,
                    "Minutes and seconds cannot exceed 59.",
                    "Invalid Time Input",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        long totalMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L;

        if (totalMillis <= 0)
        {
            JOptionPane.showMessageDialog(
                    null,
                    "Please enter a valid time duration.",
                    "Invalid Time",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        VisualTimer timer = new VisualTimer(name, totalMillis, plugin, chatMessageManager);
        timer.setPlugin(plugin);
        timer.start();
        overlayManager.addTimer(timer);
        refreshTimerList();
    }

    private void refreshTimerList()
    {
        timerListPanel.removeAll();

        List<VisualTimerOverlay> overlays = overlayManager.getActiveOverlays();

        for (VisualTimerOverlay overlay : overlays)
        {
            VisualTimer timer = overlay.getTimer();

            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(backgroundColor);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            row.setPreferredSize(new Dimension(0, 32));

            JLabel label = new JLabel(timer.getName() + " — " + timer.getInitialFormattedTime());
            label.setForeground(Color.WHITE);

            JButton delete = new JButton("−");
            delete.setFocusable(false);
            delete.setPreferredSize(new Dimension(40, 24));
            delete.addActionListener(e -> {
                overlayManager.removeTimer(overlay);
                refreshTimerList();
            });

            row.add(label, BorderLayout.CENTER);
            row.add(delete, BorderLayout.EAST);
            timerListPanel.add(row);
        }

        timerListPanel.revalidate();
        timerListPanel.repaint();
    }

    private int parseTime(String text)
    {
        try
        {
            return Math.max(0, Integer.parseInt(text));
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    private void styleTextField(JTextField field)
    {
        field.setBackground(inputBackground);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(null);
        field.setFont(new Font("Arial", Font.PLAIN, 16));
    }

    private void styleButton(JButton button, String hexColor)
    {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusable(false);
        button.setBackground(Color.decode(hexColor));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }

    private JLabel makeCenteredLabel(String text)
    {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    public void updatePreviewTimer()
    {
        if (overlayManager.getActiveOverlays().isEmpty())
        {
            previewLabel.setText("0:00:00.0");
            return;
        }

        // Just show the first active timer's time
        VisualTimerOverlay firstOverlay = overlayManager.getActiveOverlays().get(0);
        VisualTimer timer = firstOverlay.getTimer();
        previewLabel.setText(timer.getFormattedTime());
    }
}