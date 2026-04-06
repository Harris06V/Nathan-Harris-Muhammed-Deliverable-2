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

public class LoginPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPanel(GameClient client) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel loginBox = new JPanel(new GridLayout(4, 2, 5, 5));

        loginBox.add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        loginBox.add(usernameField);

        loginBox.add(new JLabel("Password:"));
        passwordField = new JPasswordField(15);
        loginBox.add(passwordField);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        loginBox.add(loginBtn);
        loginBox.add(registerBtn);

        loginBox.add(new JLabel(""));
        loginBox.add(new JLabel(""));

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(loginBox, gbc);

        loginBtn.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter both username and password.");
                return;
            }
            Map<String, Object> result = client.getApi().login(user, pass);
            if (result.containsKey("error")) {
                JOptionPane.showMessageDialog(this, result.get("error"), "Login Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                long pid = ((Number) result.get("playerId")).longValue();
                client.setPlayerId(pid);
                client.setUsername(result.get("username").toString());
                usernameField.setText("");
                passwordField.setText("");
                client.showPanel("Menu");
            }
        });

        registerBtn.addActionListener(e -> client.showPanel("Register"));
    }
}
