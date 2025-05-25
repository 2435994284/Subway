import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {
    private SubwayNetwork network;
    
    // 界面组件
    private JComboBox<String> startStationCombo;
    private JComboBox<String> endStationCombo;
    private JButton searchButton;
    private JButton clearButton;
    private JList<String> pathList;
    private DefaultListModel<String> pathListModel;
    private JTextArea journeyGuideArea;
    private JPanel ticketPanel;
    private JRadioButton singleJourneyRadio;
    private JRadioButton wuhanTongRadio;
    private JRadioButton oneDayPassRadio;
    private JRadioButton threeDayPassRadio;
    private JRadioButton sevenDayPassRadio;
    private JLabel fareLabel;
    private JLabel distanceLabel;
    
    // 当前选择的路径
    private List<List<String>> currentPaths;
    private List<String> selectedPath;
    
    public MainFrame(SubwayNetwork network) {
        this.network = network;
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("武汉地铁查询系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // 创建主面板，使用BorderLayout布局
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 创建查询面板
        JPanel queryPanel = createQueryPanel();
        mainPanel.add(queryPanel, BorderLayout.NORTH);
        
        // 创建中央面板，使用分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        
        // 路径列表面板
        JPanel pathListPanel = createPathListPanel();
        splitPane.setLeftComponent(pathListPanel);
        
        // 路径详情面板
        JPanel pathDetailPanel = createPathDetailPanel();
        splitPane.setRightComponent(pathDetailPanel);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // 添加底部状态栏
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("路径查询"));
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        // 获取所有站点名称并排序
        Vector<String> stationNames = new Vector<>(network.getStations().keySet());
        Collections.sort(stationNames);
        
        // 创建起点站下拉框
        JLabel startLabel = new JLabel("起点站：");
        startStationCombo = new JComboBox<>(stationNames);
        startStationCombo.setEditable(true);
        inputPanel.add(startLabel);
        inputPanel.add(startStationCombo);
        
        // 创建终点站下拉框
        JLabel endLabel = new JLabel("终点站：");
        endStationCombo = new JComboBox<>(stationNames);
        endStationCombo.setEditable(true);
        inputPanel.add(endLabel);
        inputPanel.add(endStationCombo);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchButton = new JButton("查询");
        clearButton = new JButton("清除");
        
        // 添加查询按钮事件
        searchButton.addActionListener(e -> searchPaths());
        
        // 添加清除按钮事件
        clearButton.addActionListener(e -> {
            startStationCombo.setSelectedIndex(-1);
            endStationCombo.setSelectedIndex(-1);
            pathListModel.clear();
            journeyGuideArea.setText("");
            fareLabel.setText("票价：0元");
            distanceLabel.setText("总距离：0.0公里");
            currentPaths = null;
            selectedPath = null;
        });
        
        buttonPanel.add(searchButton);
        buttonPanel.add(clearButton);
        
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createPathListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("路径列表"));
        
        pathListModel = new DefaultListModel<>();
        pathList = new JList<>(pathListModel);
        pathList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 添加选择事件
        pathList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && pathList.getSelectedIndex() != -1 && currentPaths != null) {
                int index = pathList.getSelectedIndex();
                if (index >= 0 && index < currentPaths.size()) {
                    selectedPath = currentPaths.get(index);
                    updatePathDetails();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(pathList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPathDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("路径详情"));
        
        // 乘车指南文本区域
        journeyGuideArea = new JTextArea();
        journeyGuideArea.setEditable(false);
        journeyGuideArea.setLineWrap(true);
        journeyGuideArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(journeyGuideArea);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 票价选择面板
        ticketPanel = createTicketPanel();
        panel.add(ticketPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTicketPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBorder(BorderFactory.createTitledBorder("票价类型"));
        
        // 创建票价类型选择按钮组
        ButtonGroup ticketGroup = new ButtonGroup();
        
        singleJourneyRadio = new JRadioButton("单程票");
        wuhanTongRadio = new JRadioButton("武汉通(9折)");
        oneDayPassRadio = new JRadioButton("1日票(18元)");
        threeDayPassRadio = new JRadioButton("3日票(45元)");
        sevenDayPassRadio = new JRadioButton("7日票(90元)");
        
        // 添加到按钮组
        ticketGroup.add(singleJourneyRadio);
        ticketGroup.add(wuhanTongRadio);
        ticketGroup.add(oneDayPassRadio);
        ticketGroup.add(threeDayPassRadio);
        ticketGroup.add(sevenDayPassRadio);
        
        // 默认选择单程票
        singleJourneyRadio.setSelected(true);
        
        // 为票价类型添加事件
        ActionListener ticketTypeListener = e -> updateFareDisplay();
        
        singleJourneyRadio.addActionListener(ticketTypeListener);
        wuhanTongRadio.addActionListener(ticketTypeListener);
        oneDayPassRadio.addActionListener(ticketTypeListener);
        threeDayPassRadio.addActionListener(ticketTypeListener);
        sevenDayPassRadio.addActionListener(ticketTypeListener);
        
        // 添加到面板
        panel.add(singleJourneyRadio);
        panel.add(wuhanTongRadio);
        panel.add(oneDayPassRadio);
        panel.add(threeDayPassRadio);
        panel.add(sevenDayPassRadio);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EtchedBorder());
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        fareLabel = new JLabel("票价：0元");
        distanceLabel = new JLabel("总距离：0.0公里");
        
        infoPanel.add(fareLabel);
        infoPanel.add(new JSeparator(SwingConstants.VERTICAL));
        infoPanel.add(distanceLabel);
        
        panel.add(infoPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private void searchPaths() {
        String startStation = startStationCombo.getSelectedItem().toString().trim();
        String endStation = endStationCombo.getSelectedItem().toString().trim();
        
        if (startStation.isEmpty() || endStation.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择起点站和终点站", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            // 查询所有路径
            currentPaths = network.findAllPaths(startStation, endStation);
            
            if (currentPaths.isEmpty()) {
                JOptionPane.showMessageDialog(this, "没有找到从 " + startStation + " 到 " + endStation + " 的路径", 
                    "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // 更新路径列表
            updatePathList();
            
            // 选择第一条路径
            pathList.setSelectedIndex(0);
            
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updatePathList() {
        pathListModel.clear();
        
        for (int i = 0; i < currentPaths.size(); i++) {
            List<String> path = currentPaths.get(i);
            String pathText = String.format("路径 %d (%d站): %s → %s", 
                i + 1, 
                path.size() - 1,
                path.get(0), 
                path.get(path.size() - 1));
            pathListModel.addElement(pathText);
        }
    }
    
    private void updatePathDetails() {
        if (selectedPath == null) {
            return;
        }
        
        // 计算距离
        double distance = network.calculatePathDistance(selectedPath);
        distanceLabel.setText(String.format("总距离：%.2f公里", distance));
        
        // 更新乘车指南
        StringBuilder guide = new StringBuilder();
        guide.append("=== 乘车路线指南 ===\n");
        
        String currentLine = null;
        String startStation = selectedPath.get(0);
        String currentStation = startStation;
        
        for (int i = 1; i < selectedPath.size(); i++) {
            String nextStation = selectedPath.get(i);
            
            // 确定当前站点和下一站点之间的线路
            String connectionLine = findConnectionLine(currentStation, nextStation);
            
            // 如果线路改变，说明需要换乘
            if (currentLine != null && !connectionLine.equals(currentLine)) {
                guide.append(String.format("乘坐%s，从%s站到%s站\n", currentLine, startStation, currentStation));
                startStation = currentStation; // 换乘点成为新的起点
            }
            
            // 更新当前线路
            currentLine = connectionLine;
            
            // 如果是路径的最后一站，需要打印最后一段乘车路线
            if (i == selectedPath.size() - 1) {
                guide.append(String.format("乘坐%s，从%s站到%s站\n", currentLine, startStation, nextStation));
            }
            
            currentStation = nextStation;
        }
        
        guide.append(String.format("=== 全程共经过%d站 ===", selectedPath.size()));
        
        journeyGuideArea.setText(guide.toString());
        
        // 更新票价
        updateFareDisplay();
    }
    
    private String findConnectionLine(String stationA, String stationB) {
        // 获取两个站点所在的所有线路
        Set<String> linesA = network.getStations().get(stationA).getLines();
        Set<String> linesB = network.getStations().get(stationB).getLines();
        
        // 查找两站点的共同线路
        Set<String> commonLines = new HashSet<>(linesA);
        commonLines.retainAll(linesB);
        
        // 如果有共同线路，返回第一条
        if (!commonLines.isEmpty()) {
            return commonLines.iterator().next();
        }
        
        return "未知线路";
    }
    
    private void updateFareDisplay() {
        if (selectedPath == null) {
            fareLabel.setText("票价：0元");
            return;
        }
        
        TicketType ticketType;
        if (singleJourneyRadio.isSelected()) {
            ticketType = TicketType.SINGLE_JOURNEY;
        } else if (wuhanTongRadio.isSelected()) {
            ticketType = TicketType.WUHAN_TONG;
        } else if (oneDayPassRadio.isSelected()) {
            ticketType = TicketType.ONE_DAY_PASS;
        } else if (threeDayPassRadio.isSelected()) {
            ticketType = TicketType.THREE_DAY_PASS;
        } else if (sevenDayPassRadio.isSelected()) {
            ticketType = TicketType.SEVEN_DAY_PASS;
        } else {
            ticketType = TicketType.SINGLE_JOURNEY;
        }
        
        int fare = network.calculateFare(selectedPath, ticketType);
        
        // 对于日票，显示日票价格和单次乘车费用
        if (ticketType == TicketType.ONE_DAY_PASS || 
            ticketType == TicketType.THREE_DAY_PASS ||
            ticketType == TicketType.SEVEN_DAY_PASS) {
            int dayPassPrice = network.getDayPassPrice(ticketType);
            fareLabel.setText(String.format("票价：%d元 (需先购买%d元日票)", fare, dayPassPrice));
        } else {
            fareLabel.setText(String.format("票价：%d元", fare));
        }
    }
}
