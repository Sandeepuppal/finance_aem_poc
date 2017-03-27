package com.hsbc.mods.finance.models

/**
  * Created by Bala on 3/21/17.
  */
object Models {

  case class MSDList(mSDList: Seq[FinanceException])

  case class FinanceException(componentName:String, sourceFunction:String, exceptionAttribute:String, uniqueKeyIdentifier:String,uniqueKeyInstanceId:String, autoAction:String, exceptionCategory:String, exceptionType:String, exceptionSeverity:String)
  case class EnrichedFinanceException(componentName:String, sourceFunction:String, exceptionAttribute:String, uniqueKeyIdentifier:String, uniqueKeyInstanceId:String, autoAction:String, exceptionCategory:String, exceptionType:String, exceptionSeverity:String, exceptionCode:String, exceptionMessage:String)

  case class Asset (ruid: String)

}
