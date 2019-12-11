package io.demo.bank.controller.web;


import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import io.demo.bank.model.CreditCardApplication;
import io.demo.bank.model.CreditCardBillingDetail;
import io.demo.bank.model.CreditCardDetail;
import io.demo.bank.model.CreditCardReference;
import io.demo.bank.model.CreditCardTransaction;
import io.demo.bank.model.security.Users;
import io.demo.bank.service.CreditCardService;
import io.demo.bank.service.UserService;
import io.demo.bank.util.Constants;



@Controller
@RequestMapping(Constants.URI_CREDIT)
public class WebCreditController extends WebCommonController {
	
	// Class Logger
	private static final Logger LOG = LoggerFactory.getLogger(WebCreditController.class);
		
	@Autowired
	private UserService userService;
	
	@Autowired
	private CreditCardService ccService;
	
	
	
	@GetMapping(Constants.URI_CREDIT_APP)
	public String getCreditApp(Principal principal, Model model) {
		
		// Set Display Defaults
		setDisplayDefaults(principal, model);
		
		// Pass in user profile details to prefill application
		Users user = userService.findByUsername(principal.getName());
		
		model.addAttribute(MODEL_CREDIT_APP, new CreditCardApplication(user.getUserProfile()));
		    
		return Constants.VIEW_CREDIT_APP;
	}
	
	@PostMapping(Constants.URI_CREDIT_APP)
	public String postCreditApp(Principal principal, Model model,
								@ModelAttribute(MODEL_CREDIT_APP) CreditCardApplication app) {
		
		// Set Display Defaults
		setDisplayDefaults(principal, model);
		
		Users user = userService.findByUsername(principal.getName());
		
		ccService.submitCreditApplication(user, app);
		
		return Constants.DIR_REDIRECT + Constants.URI_CREDIT + Constants.URI_CREDIT_APP_STATUS;
	}
	
	@GetMapping(Constants.URI_CREDIT_APP_STATUS)
	public String getCreditAppStatus(Principal principal, Model model) {
		
		// Set Display Defaults
		setDisplayDefaults(principal, model);
		
		Users user = userService.findByUsername(principal.getName());
		
		CreditCardReference ccReference = ccService.getCurrentCreditAppStatus(user);
		
		model.addAttribute(MODEL_CREDIT_CC_REFERENCE, ccReference);
		
		
		    
		return Constants.VIEW_CREDIT_APP_ST;
	}
	
	@GetMapping(Constants.URI_CREDIT_VIEW)
	public String getCreditView(Principal principal, Model model) {
		
		// Set Display Defaults
		setDisplayDefaults(principal, model);
		
		Users user = userService.findByUsername(principal.getName());
		CreditCardReference ccReference = ccService.getCurrentCreditAppStatus(user);
		CreditCardDetail ccDetail = ccService.getCreditCardDetails(ccReference.getCreditCardId());
		CreditCardBillingDetail ccBillingDetail = ccService.getCreditCardBillingDetail(ccReference.getCreditCardId());
		List<CreditCardTransaction> ccTransDetailList = ccService.getCreditCardTransactions(ccReference.getCreditCardId());
		
		LOG.debug("Credit Card Detail: " + ccDetail);
		LOG.debug("Credit Card Billing Detail: " + ccBillingDetail);
		LOG.debug("Credit Card Transaction List: " + ccTransDetailList);
		
		if (ccDetail != null && ccBillingDetail != null && ccTransDetailList != null) {
			
			model.addAttribute(MODEL_CREDIT_CC_MASKED_NO, Constants.CREDIT_CARD_NO_MASK + ccDetail.getCardNumber().substring(12));			
			model.addAttribute(MODEL_CREDIT_CC_DETAIL, ccDetail);
			model.addAttribute(MODEL_CREDIT_CC_BILLING_DETAIL, ccBillingDetail);
			model.addAttribute(MODEL_CREDIT_CC_TRANS_DETAIL, ccTransDetailList);
			
			return Constants.VIEW_CREDIT_VIEW;
			
			
		} else {
			
			// return for a new credit application
			model.addAttribute(MODEL_CREDIT_APP, new CreditCardApplication(user.getUserProfile()));
			return Constants.VIEW_CREDIT_APP;
		}  // end if

	}
	
}
