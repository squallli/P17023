////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by Fernflower decompiler)
////
//
//package com.eva;
//
//import java.util.ArrayList;
//
//public class MagReader {
//    String FlightDate = "";
//
//    public MagReader(String FlightDate) {
//        this.FlightDate = FlightDate;
//    }
//
//    public ArrayList<String> GetCardData(boolean IsCUP, String Track1, String Track2) {
//        ArrayList data = new ArrayList();
//        ArrayList ErrorMsg = new ArrayList();
//        if (Track1.length() != 0 && Track2.length() != 0) {
//            if (Track1.split("\\^").length == 3 && Track2.split("=").length == 2) {
//                String CardNo = Track2.split("=")[0].replace(" ", "");
//                String CardName = Track1.split("\\^")[1].replaceAll("\\s+$", "").replace("\'", "\'\'").replace("\\", "");
//                String CardDate = Track2.split("=")[1].substring(2, 4) + Track2.split("=")[1].substring(0, 2);
//                if (!this.CheckCardNo(CardNo)) {
//                    ErrorMsg.add("0");
//                    ErrorMsg.add("Luhn error");
//                    return ErrorMsg;
//                } else if (Integer.valueOf(CardDate.substring(2, 4)).intValue() < Integer.valueOf(this.FlightDate.substring(2, 4)).intValue()) {
//                    ErrorMsg.add("0");
//                    ErrorMsg.add("Expired card!");
//                    return ErrorMsg;
//                } else if (Integer.valueOf(CardDate.substring(2, 4)) == Integer.valueOf(this.FlightDate.substring(2, 4)) && Integer.valueOf(CardDate.substring(0, 2)).intValue() < Integer.valueOf(this.FlightDate.substring(4, 6)).intValue()) {
//                    ErrorMsg.add("0");
//                    ErrorMsg.add("Expired card!");
//                    return ErrorMsg;
//                } else {
//                    String CardType;
//                    String ServiceCode;
//                    if (IsCUP) {
//
//                        ServiceCode = "CUP";
//
//                        if (Integer.valueOf(CardNo.substring(0, 6)).intValue() >= 352800 && Integer.valueOf(CardNo.substring(0, 6)).intValue() <= 358999) {
//                            CardType = "JCB";
//                        } else if (Integer.valueOf(CardNo.substring(0, 6)).intValue() >= 400000 && Integer.valueOf(CardNo.substring(0, 6)).intValue() <= 499999) {
//                            CardType = "VISA";
//                        } else if (Integer.valueOf(CardNo.substring(0, 6)).intValue() >= 510000 && Integer.valueOf(CardNo.substring(0, 6)).intValue() <= 559999 || Integer.valueOf(CardNo.substring(0, 6)).intValue() >= 222100 && Integer.valueOf(CardNo.substring(0, 6)).intValue() <= 272099) {
//                            CardType = "MASTER";
//                        } else {
//                            CardType = "CUP";
//                        }
//
//                        if (CardNo.length() != 16) {
//                            ErrorMsg.add("0");
//                            ErrorMsg.add("This card can\'t be acceptable by off line using!");
//                            return ErrorMsg;
//                        } else {
//                            ErrorMsg.add("1");
//                            data.add(CardType);
//                            data.add(CardNo);
//                            data.add(CardName);
//                            data.add(CardDate);
//                            data.add(ServiceCode);
//                            return data;
//                        }
//                    } else {
//                        if (Integer.valueOf(CardNo.substring(0, 6)).intValue() >= 352800 && Integer.valueOf(CardNo.substring(0, 6)).intValue() <= 358999) {
//                            CardType = "JCB";
//                        } else if (Integer.valueOf(CardNo.substring(0, 6)).intValue() >= 400000 && Integer.valueOf(CardNo.substring(0, 6)).intValue() <= 499999) {
//                            CardType = "VISA";
//                        } else if (Integer.valueOf(CardNo.substring(0, 6)).intValue() >= 510000 && Integer.valueOf(CardNo.substring(0, 6)).intValue() <= 559999 || Integer.valueOf(CardNo.substring(0, 6)).intValue() >= 222100 && Integer.valueOf(CardNo.substring(0, 6)).intValue() <= 272099) {
//                            CardType = "MASTER";
//                        } else {
//                            if (!CardNo.substring(0, 2).equals("34") && !CardNo.substring(0, 2).equals("37")) {
//                                if (!CardNo.substring(0, 2).equals("36") && !CardNo.substring(0, 2).equals("38")) {
//                                    if (Integer.valueOf(CardNo.substring(0, 3)).intValue() >= 300 && Integer.valueOf(CardNo.substring(0, 3)).intValue() <= 305) {
//                                        ErrorMsg.add("0");
//                                        ErrorMsg.add("Not accept card!");
//                                        return ErrorMsg;
//                                    }
//
//                                    ErrorMsg.add("0");
//                                    ErrorMsg.add("Not accept card!");
//                                    return ErrorMsg;
//                                }
//
//                                ErrorMsg.add("0");
//                                ErrorMsg.add("Not accept card!");
//                                return ErrorMsg;
//                            }
//
//                            CardType = "AE";
//                        }
//
//                        ServiceCode = Track2.split("=")[1].substring(4, 7);
//                        if (!CardType.equals("AMX")) {
//                            if (ServiceCode.substring(0, 1).equals("5")) {
//                                ErrorMsg.add("0");
//                                ErrorMsg.add("Local card can\'t be acceptable!");
//                                return ErrorMsg;
//                            }
//
//                            if (ServiceCode.substring(0, 1).equals("6")) {
//                                ErrorMsg.add("0");
//                                ErrorMsg.add("Local card can\'t be acceptable!");
//                                return ErrorMsg;
//                            }
//
//                            if (ServiceCode.substring(0, 1).equals("7")) {
//                                ErrorMsg.add("0");
//                                ErrorMsg.add("Private card can\'t be acceptable!");
//                                return ErrorMsg;
//                            }
//
//                            if (ServiceCode.substring(0, 1).equals("9")) {
//                                ErrorMsg.add("0");
//                                ErrorMsg.add("Test card can\'t be acceptable!");
//                                return ErrorMsg;
//                            }
//
//                            if (ServiceCode.substring(0, 1).equals("0") || ServiceCode.substring(0, 1).equals("3") || ServiceCode.substring(0, 1).equals("4") || ServiceCode.substring(0, 1).equals("8")) {
//                                ErrorMsg.add("0");
//                                ErrorMsg.add("Not accept service code");
//                                return ErrorMsg;
//                            }
//
//                            if (ServiceCode.substring(1, 2).equals("2")) {
//                                ErrorMsg.add("0");
//                                ErrorMsg.add("This card can\'t be acceptable by off line using!");
//                                return ErrorMsg;
//                            }
//
//                            if (ServiceCode.substring(1, 2).equals("1") || ServiceCode.substring(1, 2).equals("3") || ServiceCode.substring(1, 2).equals("4") || ServiceCode.substring(1, 2).equals("5") || ServiceCode.substring(1, 2).equals("6") || ServiceCode.substring(1, 2).equals("7") || ServiceCode.substring(1, 2).equals("8") || ServiceCode.substring(1, 2).equals("9")) {
//                                ErrorMsg.add("0");
//                                ErrorMsg.add("Not accept service code");
//                                return ErrorMsg;
//                            }
//
//                            if (ServiceCode.substring(2, 3).equals("0")) {
//                                ErrorMsg.add("0");
//                                ErrorMsg.add("This card can\'t be acceptable by off line using!");
//                                return ErrorMsg;
//                            }
//
//                            if (ServiceCode.substring(2, 3).equals("3")) {
//                                ErrorMsg.add("0");
//                                ErrorMsg.add("ATM card can\'t be acceptable!");
//                                return ErrorMsg;
//                            }
//
//                            if (ServiceCode.substring(2, 3).equals("4")) {
//                                ErrorMsg.add("0");
//                                ErrorMsg.add("Cash card can\'t be acceptable!");
//                                return ErrorMsg;
//                            }
//
//                            if (ServiceCode.substring(2, 3).equals("5")) {
//                                ErrorMsg.add("0");
//                                ErrorMsg.add("This card can\'t be acceptable by off line using!");
//                                return ErrorMsg;
//                            }
//
//                            if (ServiceCode.substring(2, 3).equals("7") || ServiceCode.substring(2, 3).equals("8") || ServiceCode.substring(2, 3).equals("9")) {
//                                ErrorMsg.add("0");
//                                ErrorMsg.add("Not accept service code");
//                                return ErrorMsg;
//                            }
//                        }
//
//                        if ((!CardType.equals("VISA") || CardNo.length() == 16) && (!CardType.equals("MASTER") || CardNo.length() == 16) && (!CardType.equals("AE") || CardNo.length() == 15) && (!CardType.equals("DINERS") || CardNo.length() == 14) && (!CardType.equals("JCB") || CardNo.length() == 16)) {
//                            ErrorMsg.add("1");
//                            data.add(CardType);
//                            data.add(CardNo);
//                            data.add(CardName);
//                            data.add(CardDate);
//                            data.add(ServiceCode);
//                            return data;
//                        } else {
//                            ErrorMsg.add("0");
//                            ErrorMsg.add("System can\'t accept this card");
//                            return ErrorMsg;
//                        }
//                    }
//                }
//            } else {
//                ErrorMsg.add("0");
//                ErrorMsg.add("Luhn error");
//                return ErrorMsg;
//            }
//        } else {
//            ErrorMsg.add("0");
//            ErrorMsg.add("Card error");
//            return ErrorMsg;
//        }
//    }
//
//    private boolean CheckCardNo(String CardNo) {
//        int sum = 0;
//        boolean alternate = false;
//
//        for (int i = CardNo.length() - 1; i >= 0; --i) {
//            int n = Integer.parseInt(CardNo.substring(i, i + 1));
//            if (alternate) {
//                n *= 2;
//                if (n > 9) {
//                    n = n % 10 + 1;
//                }
//            }
//
//            sum += n;
//            alternate = !alternate;
//        }
//
//        return sum % 10 == 0;
//    }
//}
