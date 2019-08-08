package logic.pages;

import javafx.util.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableControlBase extends BasePage {
    WebElement element;

    public TableControlBase(WebElement element) {
        this.element = element;
    }

    public int getRowsCount() {
        return element.findElements(By.xpath(".//tr[contains(@class,'informationBoxRow')]")).size();
    }

    public int getRowsCountWithOutBoxRow() {
        return element.findElements(By.xpath("//tr[contains(@class,'informationBoxRow')]")).size();
    }

    public WebElement getRecordByIndex(int index) {
        // i = 1: Header
        return getRowByIndex(index);
    }

    public WebElement getLinkByText(String text) {
        // i = 1: Header
        try {
            WebElement row = element.findElement(By.xpath(String.format(".//tr/td//a[contains(text(),'%s')]", text)));
            return row;
        } catch (Exception e) {
            return null;
        }
    }

    public String getCellValueByColumnNameAndRowIndex(int index, String columnName) {
        // i = 1: Header
        try {
            WebElement row = getRowByIndex(index);
            int columnIndex = getColumnIndex(columnName);
            String xpath = String.format(".//td[%d]", columnIndex + 1);
            return row.findElement(By.xpath(xpath)).getText();
        } catch (Exception e) {
            return null;
        }
    }

    public String getCellAttributeValueByColumnNameAndRowIndex(int index, String columnName) {
        // i = 1: Header
        try {
            WebElement row = getRowByIndex(index);
            int columnIndex = getColumnIndex(columnName);
            String xpath = String.format(".//td[%d]//input", columnIndex + 2);
            return row.findElement(By.xpath(xpath)).getAttribute("value");
        } catch (Exception e) {
            return null;
        }
    }


    public WebElement getElementByColumnNameAndRowIndex(int index, String columnName) {
        // index = 1: Header
        try {
            WebElement row = getRowByIndex(index);
            int columnIndex = getColumnIndex(columnName);
            String xpath = String.format(".//td[%d]", columnIndex + 1);
            return row.findElement(By.xpath(xpath));
        } catch (Exception e) {
            return null;
        }
    }

    public WebElement getPasswordTextBoxByText(String text) {
        // index = 1: Header
        try {

            String xpath = String.format("//td[contains(text(),'%s')]//following-sibling::td/input[@type='password']",text);
            return element.findElement(By.xpath(xpath));
        } catch (Exception e) {
            return null;
        }
    }

    public WebElement getElementByColumnIndexAndRowIndex(int rowIndex, int columnIndex) {
        // rowIndex = 1: Header
        try {
            WebElement row = getRowByIndex(rowIndex);
            String xpath = String.format(".//td[%d]", columnIndex + 1);
            return row.findElement(By.xpath(xpath));
        } catch (Exception e) {
            return null;
        }
    }


    public WebElement getElementByColumnIndexAndRowClassName(String rowClassName, int columnIndex) {
        // rowIndex = 1: Header
        try {
            WebElement row = element.findElement(By.xpath(String.format(".//tr[@class='%s']", rowClassName)));
            String xpath = String.format(".//td[%d]", columnIndex);
            return row.findElement(By.xpath(xpath));
        } catch (Exception e) {
            return null;
        }
    }


    public WebElement getElementByColumnNameAndRowClassName(String rowClassName, String columnName, int innerIndex) {
        // i = 1: Header
        try {
            //.//table[@id='dgrdProcesses']//tr[10]//table//tr[@class='inboundRow']
            WebElement row = element.findElement(By.xpath(String.format(".//tr[@class='%s'][%d]", rowClassName, innerIndex)));
            int columnIndex = getColumnIndex(columnName);
            return row.findElement(By.xpath(String.format(".//td[%d]", columnIndex + 1)));
        } catch (Exception e) {
            return null;
        }
    }


    public WebElement getInnerRowByCellValue(String columnName, String cellValue) {
        // i = 1: Header
        try {
            WebElement row = element.findElement(By.xpath(String.format(".//tr/td[normalize-space()='%s']/parent::tr", cellValue)));
            return row;
        } catch (Exception e) {
            return null;
        }
    }

    public WebElement getRowByCellValue(String cellValue) {
        // i = 1: Header
        try {
            WebElement row = element.findElement(By.xpath(String.format(".//tr/td[normalize-space()='%s']", cellValue)));
            return row;
        } catch (Exception e) {
            return null;
        }
    }

    public WebElement getCellByLabel(String label) {
        // i = 1: Header
        try {
            String xpath=String.format(".//td[@class='label' and contains(text(),'%s')]//following-sibling::td", label);
            WebElement row = element.findElement(By.xpath(xpath));
            return row;
        } catch (Exception e) {
            return null;
        }
    }

    public WebElement getElementByCellValue(WebElement innerRow, String columnName) {
        int columnIndex = getColumnIndex(columnName);
        return innerRow.findElement(By.xpath(String.format(".//td[%d]", columnIndex + 1)));
    }


    public WebElement getElementByColumnNameAndRowClassNameAndTransName(String rowClassName, String transName) {
        // i = 1: Header
        try {
            //.//table[@id='dgrdProcesses']//tr[10]//table//tr[@class='inboundRow']
            WebElement row = element.findElement(By.xpath(String.format(".//tr[@class='%s']['Transaction']/td/a[contains(text(),'%s')]", rowClassName, transName)));
            return row;
        } catch (Exception e) {
            return null;
        }
    }


    public int getInnerTableRowCount(String rowClassName) {
        return element.findElements(By.xpath(String.format(".//tr[@class='%s']", rowClassName))).size();
    }


    public WebElement findColumnByIndex(int index) {
        List<WebElement> rows = element.findElements(By.tagName("tr"));
        for (WebElement row : rows) {
            List<WebElement> cols = row.findElements(By.xpath("td"));
            for (int i = 1; i <= cols.size(); i++) {
                String str = cols.get(i).getText().toString();
                if (i == index) {
                    return cols.get(index);
                }
            }
        }
        return null;
    }


    public WebElement findColumnsByName(String columName) {
        List<WebElement> rows = element.findElements(By.tagName("tr"));
        for (WebElement row : rows) {
            List<WebElement> cols = row.findElements(By.xpath("td"));
            for (WebElement col : cols) {
                String str = col.getText().toString();
                if (str.equals(columName)) {
                    return col;
                }
            }
        }
        return null;
    }


    public WebElement getRowByColumnNameAndCellValue(String columnName, String cellValue) {
        List<WebElement> body = getBody();
        int columnIndex = getColumnIndex(columnName);
        for (WebElement element : body) {
            if (element.findElements(By.tagName("td")).get(columnIndex).getText().equalsIgnoreCase(cellValue)) {
                return element;
            }
        }
        return null;
    }

    public WebElement getRowByContainsColumnNameAndCellValue(String columnName, String cellValue) {
        List<WebElement> body = getBody();
        int columnIndex = getColumnIndex(columnName);
        for (WebElement element : body) {
            if (element.findElements(By.tagName("td")).get(columnIndex).getText().contains(cellValue)) {
                return element;
            }
        }
        return null;
    }

    private List<WebElement> getBody() {
        return element.findElements(By.xpath(".//tr"));
    }

    private int getColumnIndex(String columnName) {
        int columnIndex = 0;
        List<WebElement> header = element.findElements(By.tagName("tr"));
        for (WebElement row : header) {
            List<WebElement> cols = row.findElements(By.xpath("td"));
            for (WebElement col : cols) {
                String str = col.getText().toString();
                if (str.equals(columnName)) {
                    columnIndex = cols.indexOf(col);
                    break;
                }
            }
        }
        return columnIndex;
    }

    private WebElement getRowByIndex(int index) {
        String xPath = String.format("./tbody/tr[%d]", index);
        try {

            return element.findElement(By.xpath(xPath));
        } catch (Exception e) {
            return null;
        }
    }

    public List<WebElement> getRowsByColumn(HashMap<String, String> columns) {
        int columnIndex = 0;
        WebElement elm = null;
        List<WebElement> column = new ArrayList<>();
        List<WebElement> body = getBody();
        for (int i = 0; i < body.size(); i++) {
            boolean Flag = true;
            for (Map.Entry mapElement : columns.entrySet()) {
                String columnName = (String) mapElement.getKey();
                String cellValue = (String) mapElement.getValue();
                columnIndex = getColumnIndex(columnName);
                String ExR = body.get(i).findElements(By.tagName("td")).get(columnIndex).getText();
                if (!body.get(i).findElements(By.tagName("td")).get(columnIndex).getText().equalsIgnoreCase(cellValue)) {
                    elm = body.get(i);
                    Flag = false;
                    break;
                }

            }
            if (Flag)
                column.add(elm);
        }
        return column;
    }


    public List<WebElement> findRowsByColumns(List<HashMap<String, String>> columns) {
        int columnIndex = 0;
        boolean flag = false;
        boolean isFail = true;
        WebElement elm = null;
        List<WebElement> column = new ArrayList<>();
        List<WebElement> body = getBody();
        for (int i = 0; i < columns.size(); i++) {
            isFail = true;
            for (Map.Entry mapElement : columns.get(i).entrySet()) {
                String columnName = (String) mapElement.getKey();
                String cellValue = (String) mapElement.getValue();
                columnIndex = getColumnIndex(columnName);
                for (WebElement el : body) {
                    if (el.findElements(By.tagName("td")).get(columnIndex).getText().equalsIgnoreCase(cellValue)) {
                        flag = true;
                        isFail = true;
                        elm = el;
                        break;
                    } else {
                        flag = false;
                        isFail = false;
                        //break;
                    }
                }
            }
            if (flag && isFail)
                column.add(elm);
        }
        return column;
    }

    public List<WebElement> findRowsByColumns(HashMap<String, String> columns) {
        int columnIndex = 0;
        boolean flag = false;
        boolean isFail = true;
        WebElement elm = null;
        List<WebElement> column = new ArrayList<>();
        List<WebElement> body = getBody();
        for (Map.Entry mapElement : columns.entrySet()) {
            String columnName = (String) mapElement.getKey();
            String cellValue = (String) mapElement.getValue();
            columnIndex = getColumnIndex(columnName);
            for (WebElement el : body) {
                if (el.findElements(By.tagName("td")).get(columnIndex).getText().equalsIgnoreCase(cellValue)) {
                    flag = true;
                    isFail = true;
                    elm = el;
                    break;
                } else {
                    flag = false;
                    isFail = false;
                    //break;
                }
            }
        }
        if (flag && isFail)
            column.add(elm);

        return column;
    }

    public List<WebElement> findRowsByColumns(Pair<String, String>... pairs) {
        int columnIndex = 0;
        boolean flag = false;
        boolean isFail = true;
        WebElement elm = null;
        List<WebElement> column = new ArrayList<>();
        List<WebElement> body = getBody();
        for (Pair<String, String> p : pairs) {
            String columnName = p.getKey();
            String cellValue = p.getValue();
            columnIndex = getColumnIndex(columnName);
            for (WebElement el : body) {
                if (el.findElements(By.tagName("td")).get(columnIndex).getText().equalsIgnoreCase(cellValue)) {
                    flag = true;
                    column.add(el);
                    //break;
                } else {
                    flag = false;
                    //break;
                }
            }
        }
        return column;
    }


    public List<WebElement> getRowsByColumnsWithIndex(String[] cellValue) {
        List<Boolean> listChecked = new ArrayList<>();
        List<WebElement> list = new ArrayList<>();
        List<WebElement> body = getBody();
        for (WebElement element : body) {
            List<WebElement> td = element.findElements(By.tagName("td"));
            for (int i = 0; i < td.size(); i++) {
                if (element.findElements(By.tagName("td")).get(i).getText().contains(cellValue[i])) {
                    listChecked.add(true);
                } else {
                    listChecked.add(false);
                }
            }
            if (listChecked.stream().filter(x -> x == false).count() < 1) {
                list.add(element);
            }
            listChecked = new ArrayList<>();
        }
        return list;
    }

    private WebElement findLabelCell(String text, int index) {
        int currentIndex = 0;
        for (WebElement label : AllLabels()) {
            if (label.getText().trim().equalsIgnoreCase(text)) {
                currentIndex++;
                if (currentIndex == index) {
                    return label;
                }
            }
        }
        return null;
    }

    private List<WebElement> AllLabels() {
        return element.findElements(By.xpath(".//td[(@class!='fieldValue' and @class!='fieldvalue') or not(@class)]"));
    }

    public WebElement findControlCellByLabel(String label, int index) {
        WebElement labelCell = findLabelCell(label, index);
        return labelCell.findElement(By.xpath(".//following-sibling::td[1]"));
    }

}
