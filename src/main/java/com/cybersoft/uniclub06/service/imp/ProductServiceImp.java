package com.cybersoft.uniclub06.service.imp;

import com.cybersoft.uniclub06.dto.ColorDTO;
import com.cybersoft.uniclub06.dto.ProductDTO;
import com.cybersoft.uniclub06.dto.SizeDTO;
import com.cybersoft.uniclub06.entity.*;
import com.cybersoft.uniclub06.repository.ProductRepository;
import com.cybersoft.uniclub06.repository.VariantRepository;
import com.cybersoft.uniclub06.request.AddProductRequest;
import com.cybersoft.uniclub06.service.FileService;
import com.cybersoft.uniclub06.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImp implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VariantRepository variantRepository;

    @Autowired
    private FileService fileService;

    @Transactional
    @Override
    public void addProduct(AddProductRequest request) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(request.name());
        productEntity.setDesc(request.desc());
        productEntity.setInfo(request.information());
        productEntity.setPrice(request.price());

        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setId(request.idBrand());

        productEntity.setBrand(brandEntity);

        ProductEntity productInserted = productRepository.save(productEntity);

        VariantEntity variantEntity = new VariantEntity();
        variantEntity.setProduct(productInserted);

        ColorEntity colorEntity = new ColorEntity();
        colorEntity.setId(request.idColor());
        variantEntity.setColor(colorEntity);

        SizeEntity sizeEntity = new SizeEntity();
        sizeEntity.setId(request.idSize());
        variantEntity.setSize(sizeEntity);
        variantEntity.setPrice(request.priceSize());
        variantEntity.setQuanity(request.quantity());
        variantEntity.setImages(request.files().getOriginalFilename());

        variantRepository.save(variantEntity);
        fileService.saveFile(request.files());

    }

    @Override
    public List<ProductDTO> getProduct(int numPage) {
//        List<ProductEntity> listProductEntity = productRepository.findAll();
        Pageable page = PageRequest.of(numPage,4);

        return productRepository.findAll(page).stream().map(item -> {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setName(item.getName());
            productDTO.setPrice(item.getPrice());
            if(item.getVariants().size() > 0){
                productDTO.setLink("http://localhost:8080/file/" + item.getVariants().get(0).getImages());
            }else{
                productDTO.setLink("");
            }

            return productDTO;
        }).toList();
    }

    @Override
    public ProductDTO getDetailProduct(int id) {
        Optional<ProductEntity> optionProductEntity = productRepository.findById(id);

        return optionProductEntity.stream().map(productEntity -> {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(productEntity.getId());
            productDTO.setName(productEntity.getName());
            productDTO.setCategories(productEntity.getProductCategories().stream().map(productCategory ->
                    productCategory.getCategory().getName()
            ).toList());

            productDTO.setSizes(productEntity.getVariants().stream().map(variantEntity -> {
                SizeDTO sizeDTO = new SizeDTO();
                sizeDTO.setId(variantEntity.getSize().getId());
                sizeDTO.setName(variantEntity.getSize().getName());

                return sizeDTO;
            }).toList());

            productDTO.setColors(productEntity.getVariants().stream().map(variantEntity -> {
                ColorDTO colorDTO = new ColorDTO();
                colorDTO.setImages(variantEntity.getImages());
                colorDTO.setName(variantEntity.getColor().getName());

                colorDTO.setSizes(productEntity.getVariants().stream().map(variantEntity1 -> {
                    SizeDTO sizeDTO = new SizeDTO();
                    sizeDTO.setId(variantEntity1.getSize().getId());
                    sizeDTO.setName(variantEntity1.getSize().getName());
                    sizeDTO.setQuantity(variantEntity1.getQuanity());
                    sizeDTO.setPrice(variantEntity1.getPrice());

                    return sizeDTO;
                }).toList());

                return colorDTO;
            }).toList());

            return productDTO;
        }).findFirst().orElseThrow(()-> new RuntimeException("Không tìm thấy dữ liệu"));

    }
}
