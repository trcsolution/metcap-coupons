/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package metcap;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sap.scco.ap.plugin.BasePlugin;
import com.sap.scco.ap.pos.dao.CDBSession;
import com.sap.scco.ap.pos.dao.CDBSessionFactory;
import com.sap.scco.ap.pos.entity.BaseEntity.EntityActions;
import com.sap.scco.ap.pos.service.ServiceFactory;
import com.sap.scco.ap.pos.service.CalculationPosService;
import com.sap.scco.ap.pos.service.ReceiptChangeNotifierPosService;
import com.sap.scco.ap.pos.service.component.listener.ReceiptChangeListener;



public class CouponsAddon extends BasePlugin implements ReceiptChangeListener {
    private static Logger logger = LoggerFactory.getLogger(CouponsAddon.class);
    private ReceiptChangeNotifierPosService notifierService;
    private CalculationPosService calculationPosService;
    private CDBSession dbSession;

    @Override
    public String getId() {
        return "MetCapCoupons";
    }

    @Override
    public String getName() {
        return "MetCap Coupons Promo";
    }
    

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public void startup() {
        this.dbSession = CDBSessionFactory.instance.createSession();
        this.notifierService =ServiceFactory.INSTANCE.getOrCreateServiceInstance(ReceiptChangeNotifierPosService.class,dbSession);
        this.calculationPosService = ServiceFactory.INSTANCE.getOrCreateServiceInstance(CalculationPosService.class,dbSession);
        notifierService.registerChangeListener(this);
        
    }

    void CalculateTax(com.sap.scco.ap.pos.entity.ReceiptEntity receipt,com.sap.scco.ap.pos.entity.SalesItemEntity salesItem)
    {
        //This function clear tax items for the sales item
         salesItem.removeAllTaxItems();

         // 2 methods to change item tax
         //1- change tax amount
        // var taxAmount=java.math.BigDecimal.valueOf(4);
        // salesItems.get(0).setTaxAmount(taxAmount);
        //2- change tax rate
        salesItem.setTaxRate(BigDecimal.valueOf(1));

        //Use this function to change tax rate code
        //salesItem.setTaxRateTypeCode()

        //now we need recalculate our transaction
        // calculationPosService.recalculateReceipt(receipt);
        calculationPosService.calculate(receipt, EntityActions.CHECK_CONS);
        
    }

    public  void onSalesItemAddedToReceipt(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt, java.util.List<com.sap.scco.ap.pos.entity.SalesItemEntity> salesItems, java.math.BigDecimal quantity) {
        CalculateTax(receipt,salesItems.get(0));
    }
    public void onSalesItemUpdated(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt, com.sap.scco.ap.pos.entity.SalesItemEntity newSalesItem, java.math.BigDecimal quantity) {
        CalculateTax(receipt,newSalesItem);

    }



    

    public void onReceiptPost(com.sap.scco.ap.pos.dao.CDBSession dbSession, com.sap.scco.ap.pos.entity.ReceiptEntity receipt) {
        
        logger.info("POST-RECEIPT EVENT");
        var cpns=receipt.getCouponAssignments();
        if(cpns.size()>0)
        {
            //Get First Coupon
            var coupon=cpns.get(0).getCoupon();
            //Print Coupon Code
            logger.info(coupon.getCode());
            //Print Coupon Key
            logger.info(coupon.getKey());
            
            
        }
        logger.info(String.valueOf(cpns.size()));
        receipt.getPaymentItems().forEach(a ->{
            logger.info(a.getDescription());
            
        });;
        
        // new SalesController(this,dbSession).postReceipt(receipt);
    }

    
}
