package xiattong.demo.es.config;

/**
 * ES消息类型枚举
 */
public enum ElasticMessageTypeEnum {

    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    ElasticMessageTypeEnum(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @param type
     * @return
     */
    public static ElasticMessageTypeEnum getType(String type) {
        for (ElasticMessageTypeEnum typeEnum : ElasticMessageTypeEnum.values()) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }
}
