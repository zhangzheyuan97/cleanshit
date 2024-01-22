package com.meritdata.dam.datapacket.plan.service.impl;

import com.meritdata.dam.datapacket.plan.acquistion.service.IMaintainService;
import com.meritdata.dam.datapacket.plan.model.vo.TreeDto;
import com.meritdata.dam.datapacket.plan.service.ITreeService;
import com.meritdata.dam.datapacket.plan.utils.Constants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * @Author fanpeng
 * @Date 2023/4/24
 * @Describe 树操作service实现类
 */

@Service
public class TreeServiceImpl implements ITreeService {

    @Autowired
    IMaintainService maintainService;


    @Override
    public List<TreeDto> getTreeListByKeyWords(String keywords, List<TreeDto> treeList) {
        if (StringUtils.isNotBlank(keywords)) {
            List<TreeDto> treeDtos = maintainService.treeList(treeList);
            List<TreeDto> treeByText = maintainService.hitPathList(treeDtos, keywords);
            treeList =  maintainService.tree("-1", treeByText);
        }
        return treeList;
    }

    /**
     * 关键字过滤后排序
     * @param treeListByKeyWords  经过关键字过滤后的List<TreeDto>（开始设计是想在过滤后排序，但是前端会有bug,输入太快，会报错。改为）
     * @param subSystemOrStandAlone   DJ只有单机，FXT_MK分系统和模块，FXT_MK_DJ分系统、模块和单机，FXT_MK_DJ_ZSJ所有的树结构
     */
    public void sortTreeDtoByKeyWords(List<TreeDto> treeListByKeyWords,String subSystemOrStandAlone) {
        if(CollectionUtils.isEmpty(treeListByKeyWords)) {
            return;
        }
        TreeDto fxtOrAloneTreeDto = treeListByKeyWords.get(0);

        TreeDto mkTreeDto = new TreeDto();
        if(!Constants.TREE_ONE.equals(subSystemOrStandAlone)) {
            mkTreeDto = treeListByKeyWords.get(1);
        }
        TreeDto djTreeDto = new TreeDto();
        if(Constants.TREE_THREE.equals(subSystemOrStandAlone) || Constants.TREE_FOUR.equals(subSystemOrStandAlone) ) {
            djTreeDto = treeListByKeyWords.get(2);
        }
        TreeDto zzzsjTreeDto = new TreeDto();
        if(Constants.TREE_FOUR.equals(subSystemOrStandAlone)) {
            zzzsjTreeDto = treeListByKeyWords.get(3);
        }

        switch (subSystemOrStandAlone) {
            case Constants.TREE_ONE : {
                for (TreeDto child : fxtOrAloneTreeDto.getChildren()) {
                    sortTreeDtoRecursion(child);
                }
                treeListByKeyWords.clear();
                treeListByKeyWords.add(fxtOrAloneTreeDto);
                break;
            }
            case Constants.TREE_TWO : {
                for (TreeDto child : fxtOrAloneTreeDto.getChildren()) {
                    sortTreeDtoRecursion(child);
                }
                for (TreeDto child : mkTreeDto.getChildren()) {
                    sortTreeDtoRecursion(child);
                }
                treeListByKeyWords.clear();
                treeListByKeyWords.add(fxtOrAloneTreeDto);
                treeListByKeyWords.add(mkTreeDto);
                break;
            }
            case Constants.TREE_THREE : {
                for (TreeDto child : fxtOrAloneTreeDto.getChildren()) {
                    sortTreeDtoRecursion(child);
                }
                for (TreeDto child : mkTreeDto.getChildren()) {
                    sortTreeDtoRecursion(child);
                }
                for (TreeDto child : djTreeDto.getChildren()) {
                    sortTreeDtoRecursion(child);
                }
                treeListByKeyWords.clear();
                treeListByKeyWords.add(fxtOrAloneTreeDto);
                treeListByKeyWords.add(mkTreeDto);
                treeListByKeyWords.add(djTreeDto);
                break;
            }
            case Constants.TREE_FOUR : {
                for (TreeDto child : fxtOrAloneTreeDto.getChildren()) {
                    sortTreeDtoRecursion(child);
                }
                for (TreeDto child : mkTreeDto.getChildren()) {
                    sortTreeDtoRecursion(child);
                }
                for (TreeDto child : djTreeDto.getChildren()) {
                    sortTreeDtoRecursion(child);
                }
                for (TreeDto child : zzzsjTreeDto.getChildren()) {
                    sortTreeDtoRecursion(child);
                }
                treeListByKeyWords.clear();
                treeListByKeyWords.add(fxtOrAloneTreeDto);
                treeListByKeyWords.add(mkTreeDto);
                treeListByKeyWords.add(djTreeDto);
                treeListByKeyWords.add(zzzsjTreeDto);
                break;
            }
        }

    }

    /**
     * 对每一层树进行排序,递归
     * @param treeDto
     */
    private void sortTreeDtoRecursion(TreeDto treeDto) {
        if(CollectionUtils.isNotEmpty(treeDto.getChildren())) {
            Collections.sort(treeDto.getChildren(), customComparator);
            //第一次进来才是批次这一层级
            for (TreeDto child : treeDto.getChildren()) {
                //正序排列
                if(CollectionUtils.isNotEmpty(child.getChildren())) {
                    Collections.sort(child.getChildren(), customComparator);
                    this.sortTreeDtoRecursion(child);
                }
            }
        }
    }


    /**
     * 按照数字在前，英文在中间，中文在后面的顺序排序
     */
    private Comparator<TreeDto> customComparator = new Comparator<TreeDto>() {
        @Override
        public int compare(TreeDto o1, TreeDto o2) {
            String s1 = o1.getText();
            String s2 = o2.getText();

            char firstChar1 = s1.charAt(0);
            char firstChar2 = s2.charAt(0);

            //判断s1和s2是否为纯数字或者数字开头
            boolean isNumeric1 = s1.matches("\\d+(\\.\\d+)?");
            boolean isNumeric2 = s2.matches("\\d+(\\.\\d+)?");
            if (isNumeric1 && !isNumeric2) {
                return -1; // 数字排在前面
            } else if (!isNumeric1 && isNumeric2) {
                return 1; // 字母排在数字后面
            } else if (isNumeric1 && isNumeric2) {
                //如果都是数字，则按照从小到大排序
                return Double.compare(Double.parseDouble(s1),Double.parseDouble(s2));
            } else if(!Character.isLetterOrDigit(firstChar1)) {
                return 1;
            } else if(!Character.isLetterOrDigit(firstChar2)) {
                return -1;
            } else {
                //其他情况按照字符串比较
                Collator collator = Collator.getInstance(Locale.CHINA);
                return collator.compare(s1,s2);
            }

        }
    };

}
