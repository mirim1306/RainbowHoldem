package project_2403;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 설정(⚙️) 버튼을 눌렀을 때 표시되는 다이얼로그입니다.
 * 요구사항에 따라 "배경음악 설정"과 "효과음 설정"만 제공합니다.
 *
 * - 배경음악: 켜기/끄기 + 볼륨 슬라이더
 * - 효과음: 켜기/끄기 + 볼륨 슬라이더
 *
 * 값은 변경 즉시 SettingsManager를 통해 파일(settings.properties)에 저장되며,
 * 배경음악은 변경 사항이 바로 반영되어 들립니다.
 */
public class SettingsDialog extends JDialog {

    public SettingsDialog(Frame owner) {
        super(owner, "설정", true);
        setSize(480, 380);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(25, 30, 20, 30));
        root.setBackground(new Color(245, 245, 250));

        JLabel title = new JLabel("설정");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(title);
        root.add(Box.createVerticalStrut(20));

        root.add(createSoundSection(
            "🎵 배경음악",
            SettingsManager.isBgmEnabled(),
            SettingsManager.getBgmVolume(),
            (enabled) -> SettingsManager.setBgmEnabled(enabled),
            (volume) -> SettingsManager.setBgmVolume(volume)
        ));

        root.add(Box.createVerticalStrut(20));

        root.add(createSoundSection(
            "🔊 효과음",
            SettingsManager.isSfxEnabled(),
            SettingsManager.getSfxVolume(),
            (enabled) -> SettingsManager.setSfxEnabled(enabled),
            (volume) -> SettingsManager.setSfxVolume(volume)
        ));

        root.add(Box.createVerticalGlue());

        JButton closeButton = new JButton("닫기");
        closeButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(closeButton);
        root.add(bottomPanel);

        setContentPane(root);
    }

    // 켜기/끄기 체크박스 + 볼륨 슬라이더로 구성된 섹션 패널 생성
    private JPanel createSoundSection(String label, boolean initialEnabled, int initialVolume,
                                       java.util.function.Consumer<Boolean> onToggle,
                                       java.util.function.Consumer<Integer> onVolumeChange) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 230), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        JCheckBox enableCheck = new JCheckBox("켜기", initialEnabled);
        enableCheck.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        enableCheck.setOpaque(false);

        headerRow.add(nameLabel, BorderLayout.WEST);
        headerRow.add(enableCheck, BorderLayout.EAST);

        panel.add(headerRow);
        panel.add(Box.createVerticalStrut(10));

        JPanel volumeRow = new JPanel(new BorderLayout(10, 0));
        volumeRow.setOpaque(false);

        JLabel volumeIcon = new JLabel("볼륨");
        volumeIcon.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JSlider volumeSlider = new JSlider(0, 100, initialVolume);
        volumeSlider.setOpaque(false);
        volumeSlider.setEnabled(initialEnabled);

        JLabel volumeValueLabel = new JLabel(initialVolume + "%");
        volumeValueLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        volumeValueLabel.setPreferredSize(new Dimension(45, 20));

        volumeRow.add(volumeIcon, BorderLayout.WEST);
        volumeRow.add(volumeSlider, BorderLayout.CENTER);
        volumeRow.add(volumeValueLabel, BorderLayout.EAST);

        panel.add(volumeRow);

        // 켜기/끄기 체크박스 동작
        enableCheck.addActionListener(e -> {
            boolean enabled = enableCheck.isSelected();
            volumeSlider.setEnabled(enabled);
            onToggle.accept(enabled);
        });

        // 슬라이더로 볼륨 조절 (드래그 중에는 표시만 갱신하고, 손을 떼면 저장)
        volumeSlider.addChangeListener(e -> {
            int value = volumeSlider.getValue();
            volumeValueLabel.setText(value + "%");
            if (!volumeSlider.getValueIsAdjusting()) {
                onVolumeChange.accept(value);
            }
        });

        return panel;
    }
}
