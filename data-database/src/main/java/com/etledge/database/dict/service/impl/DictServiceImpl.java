package com.etledge.database.dict.service.impl;

import com.etledge.common.Constants;
import com.etledge.database.dict.service.DictService;
import com.etledge.database.dict.vo.Dict;
import com.etledge.database.dict.vo.DictItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DictServiceImpl implements DictService {

    private static final Logger logger = LoggerFactory.getLogger(DictServiceImpl.class);

    // 使用分层缓存结构：第一层是字典code，第二层是字典项code到值的映射
    private static final Map<String, Map<String, String>> DICT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, List<DictItem>> DICT_ITEM_CACHE = new ConcurrentHashMap<>();

    /**
     * 前置步骤
     */
    @PostConstruct
    private void init() {
        loadDictData();
    }

    /**
     * 加载字典数据
     */
    private synchronized void loadDictData() {
        try (InputStream inputStream = DictServiceImpl.class.getClassLoader().getResourceAsStream(Constants.DICT_FILE)) {
            if (inputStream == null) {
                logger.error("字典文件未找到: {}", Constants.DICT_FILE);
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            List<Dict> dictList = objectMapper.readValue(inputStream, new TypeReference<List<Dict>>() {
            });

            // 清空缓存并重新加载
            DICT_CACHE.clear();
            DICT_ITEM_CACHE.clear();

            for (Dict dict : dictList) {
                String dictCode = dict.getDictCode();
                List<DictItem> items = dict.getList();

                // 缓存字典项列表
                DICT_ITEM_CACHE.put(dictCode, Collections.unmodifiableList(items));

                // 构建并缓存字典项值的快速映射
                Map<String, String> itemMap = items.stream()
                        .collect(Collectors.toMap(
                                DictItem::getCode,
                                DictItem::getValue,
                                (existing, replacement) -> existing)); // 处理重复键

                DICT_CACHE.put(dictCode, Collections.unmodifiableMap(itemMap));
            }

            logger.info("成功加载 {} 个字典配置", dictList.size());
        } catch (IOException e) {
            logger.error("加载字典数据失败: {}", e.getMessage(), e);
            throw new IllegalStateException("字典数据初始化失败", e);
        }
    }

    /**
     * 重新加载字典数据（可用于热更新）
     */
    private synchronized void reload() {
        logger.info("重新加载字典数据...");
        loadDictData();
    }


    /**
     * 获取字典
     *
     * @param dictCode
     * @return
     */
    @Override
    public List<DictItem> getDict(String dictCode) {
        return DICT_ITEM_CACHE.getOrDefault(dictCode, Collections.emptyList());
    }

    /**
     * 根据字典code与信息项code获取信息项value
     *
     * @param dictCode  字典编码
     * @param itemValue 字典项值
     * @return 字典项值，不存在时返回空字符串
     */
    @Override
    public String getDictItemCode(String dictCode, String itemValue) {
        List<DictItem> itemList = DICT_ITEM_CACHE.get(dictCode);
        if (itemList == null) {
            logger.warn("字典不存在: {}", dictCode);
            return "";
        }

        return itemList.stream()
                .filter(item -> itemValue.equals(item.getValue()))
                .findFirst()
                .map(DictItem::getCode)
                .orElse("");
    }
}
