package net.sf.persism;

record TableInfo(String name, String schema, ConnectionTypes connectionType) {

    @Override
    public String toString() {
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
