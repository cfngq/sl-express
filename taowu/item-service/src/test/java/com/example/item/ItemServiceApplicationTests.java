package com.example.item;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.common.result.Result;
import com.example.item.domain.dto.OrderDetailDTO;
import com.example.item.domain.po.Item;
import com.example.item.service.IItemService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RequiredArgsConstructor
class ItemServiceApplicationTests {
    private final IItemService itemService;

}
