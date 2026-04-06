package com.legendsofsw.client.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.legendsofsw.client.GameClient;

public class RegisterPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public RegisterPanel(GameClient client) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel registerBox = new JPanel(new GridLayout(5, 2, 5, 5));

        registerBox.add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        registerBox.add(usernameField);

        registerBox.add(new JLabel("Password:"));
        passwordField = new JPasswordField(15);
        registerBox.add(passwordField);

        registerBox.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField(15);
        registerBox.add(confirmPasswordField);

        JButton registerBtn = new JButton("Register");
        JButton backBtn = new JButton("Back");
        registerBox.add(registerBtn);
        registerBox.add(backBtn);

        registerBox.add(new JLabel(""));
        registerBox.add(new JLabel(""));

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(registerBox, gbc);

        registerBtn.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());
            String confirm = new String(confirmPasswordField.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter both username and password.");
                return;
            }
            if (!user.matches("^[a-zA-Z0-9]+$")) {
                JOptionPane.showMessageDialog(this, "Username must be alphanumeric.");
                return;
            }
            if (!pass.matches("^[a-zA-Z0-9 ]+$")) {
                JOptionPane.showMessageDialog(this, "Password must be alphanumeric.");
                return;
            }
            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }

            Map<String, Object> result = client.getApi().register(user, pass);
            if (result.containsKey("error")) {
                JOptionPane.showMessageDialog(this, result.get("error"), "Registration Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Account created! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                usernameField.setText("");
                passwordField.setText("");
                confirmPasswordField.setText("");
                client.showPanel("Login");
            }
        });

        backBtn.addActionListener(e -> client.showPanel("Login"));
    }
}
