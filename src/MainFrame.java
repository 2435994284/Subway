import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {
    private SubwayNetwork network;
    
    // 路径查询选项卡组件
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
    
    // 换乘站查询选项卡组件
    private JList<String> transferStationList;
    private DefaultListModel<String> transferStationListModel;
    private JTextArea transferStationDetailArea;
    
    // 附近站点查询选项卡组件
    private JComboBox<String> nearbyStationCombo;
    private JTextField distanceField;
    private JButton searchNearbyButton;
    private JList<String> nearbyStationList;
    private DefaultListModel<String> nearbyStationListModel;
    
    // 当前选择的路径
    private List<List<String>> currentPaths;
    private List<String> selectedPath;
    
    // 添加最短路径相关属性
    private List<String> shortestPath;
    private JLabel shortestPathLabel;
    private int shortestPathIndex = -1;
    
    public MainFrame(SubwayNetwork network) {
        this.network = network;
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("武汉地铁查询系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 添加路径查询选项卡
        tabbedPane.addTab("路径查询", createRouteQueryPanel());
        
        // 添加换乘站查询选项卡
        tabbedPane.addTab("换乘站查询", createTransferStationPanel());
        
        // 添加附近站点查询选项卡
        tabbedPane.addTab("附近站点查询", createNearbyStationPanel());
        
        // 设置内容面板
        setContentPane(tabbedPane);
    }
    
    // 创建路径查询面板
    private JPanel createRouteQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 创建查询面板
        JPanel queryPanel = createQueryPanel();
        panel.add(queryPanel, BorderLayout.NORTH);
        
        // 创建中央面板，使用分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        
        // 路径列表面板
        JPanel pathListPanel = createPathListPanel();
        splitPane.setLeftComponent(pathListPanel);
        
        // 路径详情面板
        JPanel pathDetailPanel = createPathDetailPanel();
        splitPane.setRightComponent(pathDetailPanel);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        // 添加底部状态栏
        JPanel statusPanel = createStatusPanel();
        panel.add(statusPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // 创建换乘站查询面板
    private JPanel createTransferStationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 创建标题标签
        JLabel titleLabel = new JLabel("武汉地铁换乘站列表");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // 创建分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        
        // 换乘站列表
        transferStationListModel = new DefaultListModel<>();
        transferStationList = new JList<>(transferStationListModel);
        transferStationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 添加列表选择事件
        transferStationList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && transferStationList.getSelectedIndex() != -1) {
                String stationName = transferStationList.getSelectedValue();
                updateTransferStationDetail(stationName);
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(transferStationList);
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("换乘站列表"));
        listPanel.add(listScrollPane, BorderLayout.CENTER);
        
        splitPane.setLeftComponent(listPanel);
        
        // 换乘站详情
        transferStationDetailArea = new JTextArea();
        transferStationDetailArea.setEditable(false);
        transferStationDetailArea.setLineWrap(true);
        transferStationDetailArea.setWrapStyleWord(true);
        
        JScrollPane detailScrollPane = new JScrollPane(transferStationDetailArea);
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("换乘站详情"));
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);
        
        splitPane.setRightComponent(detailPanel);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        // 加载换乘站数据
        loadTransferStations();
        
        return panel;
    }
    
    // 创建附近站点查询面板
    private JPanel createNearbyStationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 创建查询面板
        JPanel queryPanel = new JPanel(new BorderLayout(10, 5));
        queryPanel.setBorder(BorderFactory.createTitledBorder("附近站点查询"));
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        // 获取所有站点名称并排序
        Vector<String> stationNames = new Vector<>(network.getStations().keySet());
        Collections.sort(stationNames);
        
        // 创建站点下拉框
        JLabel stationLabel = new JLabel("选择站点：");
        nearbyStationCombo = new JComboBox<>(stationNames);
        nearbyStationCombo.setEditable(true);
        inputPanel.add(stationLabel);
        inputPanel.add(nearbyStationCombo);
        
        // 创建距离输入框
        JLabel distanceLabel = new JLabel("最大距离(公里)：");
        distanceField = new JTextField("2.0");
        inputPanel.add(distanceLabel);
        inputPanel.add(distanceField);
        
        queryPanel.add(inputPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchNearbyButton = new JButton("查询");
        
        // 添加查询按钮事件
        searchNearbyButton.addActionListener(e -> searchNearbyStations());
        
        buttonPanel.add(searchNearbyButton);
        queryPanel.add(buttonPanel, BorderLayout.EAST);
        
        panel.add(queryPanel, BorderLayout.NORTH);
        
        // 创建结果列表
        nearbyStationListModel = new DefaultListModel<>();
        nearbyStationList = new JList<>(nearbyStationListModel);
        
        JScrollPane scrollPane = new JScrollPane(nearbyStationList);
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("查询结果"));
        listPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(listPanel, BorderLayout.CENTER);
        
        return panel;
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
        
        // 添加最短路径标签
        shortestPathLabel = new JLabel("最短路径: 未查询");
        shortestPathLabel.setForeground(Color.BLUE);
        shortestPathLabel.setFont(shortestPathLabel.getFont().deriveFont(Font.BOLD));
        shortestPathLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(shortestPathLabel, BorderLayout.NORTH);
        
        pathListModel = new DefaultListModel<>();
        pathList = new JList<>(pathListModel);
        pathList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 自定义单元格渲染器以高亮显示最短路径
        pathList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                          int index, boolean isSelected, 
                                                          boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                // 如果当前项是最短路径，用浅绿色背景标记
                if (index == shortestPathIndex) {
                    if (!isSelected) {
                        c.setBackground(new Color(230, 255, 230)); // 浅绿色背景
                    }
                    Font font = getFont();
                    setFont(font.deriveFont(Font.BOLD));
                }
                return c;
            }
        });
        
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
    
    // 加载换乘站数据
    private void loadTransferStations() {
        transferStationListModel.clear();
        
        // 获取所有换乘站
        Set<Station> transferStations = network.getTransferStations();
        
        // 将换乘站名称添加到列表模型
        List<String> stationNames = new ArrayList<>();
        for (Station station : transferStations) {
            stationNames.add(station.getName());
        }
        
        // 按字母顺序排序
        Collections.sort(stationNames);
        
        // 添加到列表模型
        for (String name : stationNames) {
            transferStationListModel.addElement(name);
        }
        
        // 如果有换乘站，选择第一个
        if (!transferStationListModel.isEmpty()) {
            transferStationList.setSelectedIndex(0);
        }
    }
    
    // 更新换乘站详情
    private void updateTransferStationDetail(String stationName) {
        Station station = network.getStations().get(stationName);
        if (station != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("站点名称: ").append(stationName).append("\n\n");
            sb.append("所属线路:\n");
            
            Set<String> lines = station.getLines();
            for (String line : lines) {
                sb.append("• ").append(line).append("\n");
            }
            
            // 添加可换乘路线组合
            sb.append("\n可换乘路线组合:\n");
            Set<String> linesArray = new HashSet<>(lines);
            List<String> linesList = new ArrayList<>(linesArray);
            
            for (int i = 0; i < linesList.size(); i++) {
                for (int j = i + 1; j < linesList.size(); j++) {
                    sb.append("• ").append(linesList.get(i)).append(" ⇄ ").append(linesList.get(j)).append("\n");
                }
            }
            
            transferStationDetailArea.setText(sb.toString());
        } else {
            transferStationDetailArea.setText("未找到站点信息");
        }
    }
    
    // 查询附近站点
    private void searchNearbyStations() {
        String stationName = nearbyStationCombo.getSelectedItem().toString().trim();
        
        if (stationName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择站点", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        double maxDistance;
        try {
            maxDistance = Double.parseDouble(distanceField.getText().trim());
            if (maxDistance <= 0) {
                throw new NumberFormatException("距离必须大于0");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的距离值", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // 查询附近站点
            List<SubwayNetwork.NearbyStationInfo> nearbyStations = network.findNearbyStations(stationName, maxDistance);
            
            // 清空列表模型
            nearbyStationListModel.clear();
            
            if (nearbyStations.isEmpty()) {
                nearbyStationListModel.addElement("在 " + maxDistance + " 公里范围内没有找到站点");
            } else {
                // 按距离排序
                nearbyStations.sort(Comparator.comparingDouble(SubwayNetwork.NearbyStationInfo::getDistance));
                
                // 添加到列表模型
                for (SubwayNetwork.NearbyStationInfo info : nearbyStations) {
                    String display = String.format("%s (线路: %s, 距离: %.2f公里)", 
                        info.getStationName(), info.getLineName(), info.getDistance());
                    nearbyStationListModel.addElement(display);
                }
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
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
                shortestPathLabel.setText("最短路径: 未找到路径");
                return;
            }
            
            // 查询最短路径
            shortestPath = network.findShortestPath(startStation, endStation);
            
            // 找到最短距离的路径
            shortestPathIndex = -1;
            double shortestDistance = Double.MAX_VALUE;
            
            for (int i = 0; i < currentPaths.size(); i++) {
                List<String> path = currentPaths.get(i);
                double distance = network.calculatePathDistance(path);
                
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    shortestPathIndex = i;
                }
            }
            
            // 更新路径列表
            updatePathList();
            
            // 更新最短路径标签
            if (shortestPathIndex >= 0) {
                shortestPathLabel.setText(String.format("最短路径: 第 %d 条路径 (距离 %.2f 公里, 共 %d 站)", 
                    shortestPathIndex + 1, shortestDistance, currentPaths.get(shortestPathIndex).size() - 1));
            } else {
                shortestPathLabel.setText("最短路径: 未能确定");
            }
            
            // 选择最短路径
            pathList.setSelectedIndex(shortestPathIndex);
            pathList.ensureIndexIsVisible(shortestPathIndex);
            
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updatePathList() {
        pathListModel.clear();
        
        for (int i = 0; i < currentPaths.size(); i++) {
            List<String> path = currentPaths.get(i);
            double distance = network.calculatePathDistance(path);
            
            String pathText;
            if (i == shortestPathIndex) {
                pathText = String.format("路径 %d: %s → %s (%.2f公里, %d站) [最短]", 
                    i + 1, 
                    path.get(0),
                    path.get(path.size() - 1),
                    distance,
                    path.size() - 1);
            } else {
                pathText = String.format("路径 %d: %s → %s (%.2f公里, %d站)", 
                    i + 1, 
                    path.get(0),
                    path.get(path.size() - 1),
                    distance,
                    path.size() - 1);
            }
            
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
