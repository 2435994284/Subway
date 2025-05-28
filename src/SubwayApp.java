import javax.swing.*;

public class SubwayApp {
    public static void main(String[] args) {
        try {
            // 设置系统外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 使用SwingUtilities确保GUI在事件调度线程中创建
        SwingUtilities.invokeLater(() -> {
            try {
                // 创建地铁网络并加载数据
                SubwayNetwork network = new SubwayNetwork();
                network.loadData("f:\\Project\\Subway\\src\\subway.txt");
                
                // 创建并显示主窗口
                MainFrame frame = new MainFrame(network);
                frame.setSize(800, 600); 
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "加载地铁数据失败: " + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}
