package jp.co.metateam.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import jp.co.metateam.library.values.RentalStatus;
import lombok.extern.log4j.Log4j2;

/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {

    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;

    @Autowired
    public RentalManageController(
        AccountService accountService, 
        RentalManageService rentalManageService, 
        StockService stockService
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }

    /**
     * 貸出一覧画面初期表示
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
        List<RentalManage> rentalManageList = this.rentalManageService.findAll();
       
        // 貸出一覧画面に渡すデータをmodelに追加
        model.addAttribute("rentalManageList", rentalManageList);

        return "rental/index";
    }

 
    @GetMapping("/rental/add")
    public String add(Model model) {
        List<Account> accountList = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findAll();

        model.addAttribute("accounts", accountList);
        model.addAttribute("stockList", stockList);

        model.addAttribute("rentalStatus", RentalStatus.values());

         if (!model.containsAttribute("rentalManageDto")) {
             model.addAttribute("rentalManageDto", new RentalManageDto());
         }
 
         return "rental/add";
     }
 
 
    @PostMapping("/rental/add")
    public String register(@ModelAttribute RentalManageDto RentalManageDto, BindingResult Result, RedirectAttributes ra, Model Model) {
        try {
 
            if (Result.hasErrors()) {
                throw new Exception("Validation error.");
            }
 
            // 登録処理
            this.rentalManageService.save(RentalManageDto);
           
            return "redirect:/rental/index";
 
        } catch (Exception e) {
            log.error(e.getMessage());
 
            ra.addFlashAttribute("rentalManageDto", RentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", Result);
 
            return "redirect:/rental/add";
        }
 
 
    }

    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model) {
        List<Account> accountList = this.accountService.findAll();
        List<Stock> stockList = this.stockService.findAll();

        model.addAttribute("accounts", accountList);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());

         if (!model.containsAttribute("rentalManageDto")) {
             model.addAttribute("rentalManageDto", new RentalManageDto());
            
             RentalManageDto rentalManageDto = new RentalManageDto();
             RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));

             rentalManageDto.setId(rentalManage.getId());
             rentalManageDto.setStockId(rentalManage.getStock().getId());
             rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
             rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
             rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
             rentalManageDto.setStatus(rentalManage.getStatus());

             model.addAttribute("rentalManageDto", rentalManageDto);
         }
        return "rental/edit";
    }
    
    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model) {
        try {
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            String validerror = rentalManageDto.validateStatus(rentalManage.getStatus());

            if (validerror != null) {
                result.addError(new FieldError("rentalManageDto", "status", validerror));
            }

            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            rentalManageService.update (Long.valueOf(id), rentalManageDto);

            return "redirect:/rental/index";

        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

            List<Account> accountList = this.accountService.findAll();
            List<Stock> stockList = this.stockService.findAll();

            model.addAttribute("accounts", accountList);
            model.addAttribute("stockList", stockList);
            model.addAttribute("rentalStatus", RentalStatus.values());    

            return "rental/edit";
        }
    }

}


