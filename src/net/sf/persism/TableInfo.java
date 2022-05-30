package net.sf.persism;

record TableInfo(String name, String schema) {

    @Override
    public String toString() {
        if (Util.isNotEmpty(schema)) {
            return schema + "." + name;
        }
        return name;
    }

    public String toString(ConnectionTypes connectionType) {
        StringBuilder sb = new StringBuilder();
        String sd = connectionType.getKeywordStartDelimiter();
        String ed = connectionType.getKeywordEndDelimiter();

        if (Util.isNotEmpty(schema)) {
            sb.append(sd).append(schema).append(ed).append(".");
        }
        sb.append(sd).append(name).append(ed);
        return sb.toString();
    }
}
