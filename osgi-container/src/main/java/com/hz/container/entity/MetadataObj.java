package com.hz.container.entity;

import java.util.List;

/**
 * @author zehua
 * @date 2021/5/3 14:59
 */
public class MetadataObj {
    private String jarPath;
    private List<String> importPkNames;
    private List<String> exportPkName;

    public MetadataObj(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public List<String> getImportPkNames() {
        return importPkNames;
    }

    public void setImportPkNames(List<String> importPkNames) {
        this.importPkNames = importPkNames;
    }

    public List<String> getExportPkName() {
        return exportPkName;
    }

    public void setExportPkName(List<String> exportPkName) {
        this.exportPkName = exportPkName;
    }
}
