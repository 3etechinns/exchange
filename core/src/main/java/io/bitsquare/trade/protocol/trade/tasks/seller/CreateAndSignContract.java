/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.trade.protocol.trade.tasks.seller;

import io.bitsquare.common.taskrunner.TaskRunner;
import io.bitsquare.trade.Contract;
import io.bitsquare.trade.Trade;
import io.bitsquare.trade.protocol.trade.TradeTask;
import io.bitsquare.util.Utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateAndSignContract extends TradeTask {
    private static final Logger log = LoggerFactory.getLogger(CreateAndSignContract.class);

    public CreateAndSignContract(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void doRun() {
        try {
            assert processModel.getTakeOfferFeeTxId() != null;
            Contract contract = new Contract(
                    processModel.getOffer(),
                    trade.getTradeAmount(),
                    processModel.getTakeOfferFeeTxId(),
                    processModel.tradingPeer.getAccountId(),
                    processModel.getAccountId(),
                    processModel.tradingPeer.getFiatAccount(),
                    processModel.getFiatAccount(),
                    processModel.tradingPeer.getPubKeyRing(),
                    processModel.getPubKeyRing());
            String contractAsJson = Utilities.objectToJson(contract);
            String signature = processModel.getCryptoService().signMessage(processModel.getRegistrationKeyPair(), contractAsJson);

            trade.setContract(contract);
            trade.setContractAsJson(contractAsJson);
            trade.setSellerContractSignature(signature);

            complete();
        } catch (Throwable t) {
            t.printStackTrace();
            trade.setThrowable(t);
            failed(t);
        }
    }
}