package com.nobtg.Utils;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class Main {
    public static void main(String @NotNull [] args) {
        SwingUtilities.invokeLater(GradientText::createAndShowGui);
    }
}