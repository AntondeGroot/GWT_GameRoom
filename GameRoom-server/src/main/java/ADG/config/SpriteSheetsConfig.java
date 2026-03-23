package ADG.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sprite-sheets")
public class SpriteSheetsConfig {

    public static class SheetDef {
        private String url;
        private int cols = 4;
        private int rows = 4;
        private int imgWidth  = 1024;
        private int imgHeight = 1024;
        private int insetPx   = 0;
        /** 1-based indices to hide; all others are shown. Ignored when onlyInclude is non-empty. */
        private List<Integer> exclude     = new ArrayList<>();
        /** 1-based indices to show; all others are hidden. Takes precedence over exclude. */
        private List<Integer> onlyInclude = new ArrayList<>();

        public String         getUrl()         { return url; }
        public int            getCols()        { return cols; }
        public int            getRows()        { return rows; }
        public int            getImgWidth()    { return imgWidth; }
        public int            getImgHeight()   { return imgHeight; }
        public int            getInsetPx()     { return insetPx; }
        public List<Integer>  getExclude()     { return exclude; }
        public List<Integer>  getOnlyInclude() { return onlyInclude; }

        public void setUrl(String url)                      { this.url = url; }
        public void setCols(int cols)                       { this.cols = cols; }
        public void setRows(int rows)                       { this.rows = rows; }
        public void setImgWidth(int imgWidth)               { this.imgWidth = imgWidth; }
        public void setImgHeight(int imgHeight)             { this.imgHeight = imgHeight; }
        public void setInsetPx(int insetPx)                 { this.insetPx = insetPx; }
        public void setExclude(List<Integer> exclude)       { this.exclude = exclude; }
        public void setOnlyInclude(List<Integer> onlyInclude) { this.onlyInclude = onlyInclude; }
    }

    private List<SheetDef> sheets = new ArrayList<>();

    public List<SheetDef> getSheets() { return sheets; }
    public void setSheets(List<SheetDef> sheets) { this.sheets = sheets; }
}