package com.java2nb.novel.core.result;

public class ElasticSearchConstant {
    public static final String INDEX_NAME = "book";
    public static final String INDEX_TEMPLATE =
            "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"long\"\n" +
            "      },\n" +
            "      \"bookName\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"copy_to\": \"keywordSearch\"\n" +
            "      },\n" +
            "      \"catId\": {\n" +
            "        \"type\": \"long\"\n" +
            "      },\n" +
            "      \"catName\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"copy_to\": \"keywordSearch\"\n" +
            "      },\n" +
            "      \"lastIndexId\": {\n" +
            "        \"type\": \"long\"\n" +
            "      },\n" +
            "      \"lastIndexName\": {\n" +
            "        \"type\": \"text\"\n" +
            "      },\n" +
            "      \"authorName\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"copy_to\": \"keywordSearch\"\n" +
            "      },\n" +
            "      \"wordCount\": {\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"updateTime\": {\n" +
            "        \"type\": \"date\"\n" +
            "      },\n" +
            "      \"visitCount\": {\n" +
            "        \"type\": \"long\"\n" +
            "      },\n" +
            "      \"bookStatus\": {\n" +
            "        \"type\": \"byte\"\n" +
            "      },\n" +
            "      \"bookDesc\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"copy_to\": \"keywordSearch\"\n" +
            "      },\n" +
            "      \"keywordSearch\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_smart\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
