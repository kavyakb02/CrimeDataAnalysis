import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class CrimeDataAnalysis2 {

    static class CrimeRecord {
        String crimeHead;
        int year;
        int incidents;

        CrimeRecord(String crimeHead, int year, int incidents) {
            this.crimeHead = crimeHead;
            this.year = year;
            this.incidents = incidents;
        }
    }

    public static List<CrimeRecord> readCSV(String filePath) {
        List<CrimeRecord> records = new ArrayList<>();
        String line = "";
        String splitBy = ",";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip the header line
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(splitBy);
                try {
                    String crimeHead = data[0].trim(); // Assuming the first column is the crime head
                    for (int i = 1; i < data.length; i++) {
                        int year = 2001 + i - 1; // Assuming years start from 2001 and are consecutive
                        int incidents = Integer.parseInt(data[i].trim());
                        records.add(new CrimeRecord(crimeHead, year, incidents));
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid record: " + Arrays.toString(data));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    public static class UserInputFrame extends JFrame implements ActionListener {
        JTextField year1Field, year2Field, crimeHeadField;
        JButton compareButton, uploadButton, visualizeButton, trendButton;
        List<CrimeRecord> records;
        String filePath;

        UserInputFrame() {
            setLayout(new FlowLayout());

            uploadButton = new JButton("Upload CSV");
            add(uploadButton);
            uploadButton.addActionListener(this);

            add(new JLabel("Enter Crime Head:"));
            crimeHeadField = new JTextField(15);
            add(crimeHeadField);

            add(new JLabel("Enter first year:"));
            year1Field = new JTextField(5);
            add(year1Field);

            add(new JLabel("Enter second year:"));
            year2Field = new JTextField(5);
            add(year2Field);

            compareButton = new JButton("Compare");
            add(compareButton);
            compareButton.addActionListener(this);

            visualizeButton = new JButton("Visualize Data");
            add(visualizeButton);
            visualizeButton.addActionListener(this);

            trendButton = new JButton("Analyze Trend");
            add(trendButton);
            trendButton.addActionListener(this);

            setSize(600, 300);
            setTitle("Crime Data Analysis");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setVisible(true);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == uploadButton) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    filePath = selectedFile.getAbsolutePath();
                    records = readCSV(filePath);
                    showMessageDialog("File uploaded successfully!");
                }
            } else if (e.getSource() == compareButton) {
                try {
                    if (records == null || records.isEmpty()) {
                        showErrorDialog("Please upload a CSV file first.");
                        return;
                    }
                    String crimeHead = crimeHeadField.getText().trim();
                    int year1 = Integer.parseInt(year1Field.getText());
                    int year2 = Integer.parseInt(year2Field.getText());
                    performComparison(crimeHead, year1, year2);
                } catch (NumberFormatException ex) {
                    showErrorDialog("Please enter valid years.");
                }
            } else if (e.getSource() == visualizeButton) {
                if (records == null || records.isEmpty()) {
                    showErrorDialog("Please upload a CSV file first.");
                    return;
                }
                visualizeData();
            } else if (e.getSource() == trendButton) {
                if (records == null || records.isEmpty()) {
                    showErrorDialog("Please upload a CSV file first.");
                    return;
                }
                analyzeTrend();
            }
        }

        private void performComparison(String crimeHead, int year1, int year2) {
            List<CrimeRecord> recordsYear1 = records.stream()
                    .filter(r -> r.crimeHead.equalsIgnoreCase(crimeHead) && r.year == year1)
                    .collect(Collectors.toList());

            List<CrimeRecord> recordsYear2 = records.stream()
                    .filter(r -> r.crimeHead.equalsIgnoreCase(crimeHead) && r.year == year2)
                    .collect(Collectors.toList());

            if (recordsYear1.isEmpty() || recordsYear2.isEmpty()) {
                showErrorDialog("Data for the specified crime head and years is not available.");
                return;
            }

            int incidentsYear1 = recordsYear1.get(0).incidents;
            int incidentsYear2 = recordsYear2.get(0).incidents;

            String result = String.format(
                    "Crime Head: %s\n" +
                            "Year %d: Incidents: %d\n" +
                            "Year %d: Incidents: %d\n",
                    crimeHead, year1, incidentsYear1,
                    year2, incidentsYear2);

            showResultDialog(result);
        }

        private void visualizeData() {
            String crimeHead = crimeHeadField.getText().trim();

            if (crimeHead.isEmpty()) {
                showErrorDialog("Please enter a Crime Head to visualize.");
                return;
            }

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            // Filter and add data to the dataset for the specified crime head
            for (CrimeRecord record : records) {
                if (record.crimeHead.equalsIgnoreCase(crimeHead)) {
                    dataset.addValue(record.incidents, record.crimeHead, String.valueOf(record.year));
                }
            }

            // Check if the dataset is empty (i.e., no data for the specified crime head)
            if (dataset.getRowCount() == 0) {
                showErrorDialog("No data available for the specified crime head.");
                return;
            }

            // Create the chart
            JFreeChart barChart = ChartFactory.createBarChart(
                    "Crime Data Visualization for " + crimeHead,
                    "Year",
                    "Number of Incidents",
                    dataset
            );

            // Create and display the chart panel
            ChartPanel chartPanel = new ChartPanel(barChart);
            JFrame chartFrame = new JFrame("Crime Data Visualization");
            chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            chartFrame.getContentPane().add(chartPanel);
            chartFrame.pack();
            chartFrame.setVisible(true);
        }

        private void analyzeTrend() {
            String crimeHead = crimeHeadField.getText().trim();

            if (crimeHead.isEmpty()) {
                showErrorDialog("Please enter a Crime Head to analyze the trend.");
                return;
            }

            // Filter records for the specified crime head and sort by year
            List<CrimeRecord> filteredRecords = records.stream()
                    .filter(r -> r.crimeHead.equalsIgnoreCase(crimeHead))
                    .sorted(Comparator.comparingInt(r -> r.year))
                    .collect(Collectors.toList());

            if (filteredRecords.isEmpty()) {
                showErrorDialog("No data available for the specified crime head.");
                return;
            }

            // Calculate the trend by finding the average increase/decrease in incidents
            int totalDifference = 0;
            for (int i = 1; i < filteredRecords.size(); i++) {
                int difference = filteredRecords.get(i).incidents - filteredRecords.get(i - 1).incidents;
                totalDifference += difference;
            }
            double averageDifference = (double) totalDifference / (filteredRecords.size() - 1);

            String trendResult = String.format("Trend Analysis for Crime Head: %s\n", crimeHead);
            if (averageDifference > 0) {
                trendResult += String.format("On average, the number of incidents is increasing by %.2f per year.", averageDifference);
            } else if (averageDifference < 0) {
                trendResult += String.format("On average, the number of incidents is decreasing by %.2f per year.", Math.abs(averageDifference));
            } else {
                trendResult += "The number of incidents is stable over the years.";
            }

            showResultDialog(trendResult);
        }

        private void showErrorDialog(String message) {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        }

        private void showResultDialog(String message) {
            JOptionPane.showMessageDialog(this, message, "Result", JOptionPane.INFORMATION_MESSAGE);
        }

        private void showMessageDialog(String message) {
            JOptionPane.showMessageDialog(this, message, "Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserInputFrame());
}
}
