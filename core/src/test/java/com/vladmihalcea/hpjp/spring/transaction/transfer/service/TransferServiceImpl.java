package com.vladmihalcea.hpjp.spring.transaction.transfer.service;

import com.vladmihalcea.hpjp.spring.transaction.transfer.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Vlad Mihalcea
 */
@Service
public class TransferServiceImpl implements TransferService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional
    public boolean transfer(String fromIban, String toIban, long cents) {
        boolean status = true;

        long fromBalance = accountRepository.getBalance(fromIban);

        if(fromBalance >= cents) {
            status &= accountRepository.addBalance(fromIban, (-1) * cents) > 0;
            status &= accountRepository.addBalance(toIban, cents) > 0;
        }

        return status;
    }

    //Using optimistic locking to fix the problem
    /*@Override
    @Transactional
    public boolean transfer(String fromIban, String toIban, long cents) {
        boolean status = true;

        Account fromAccount = accountRepository.findById(fromIban).orElse(null);
        Account toAccount = accountRepository.findById(toIban).orElse(null);
        long fromBalance = fromAccount.getBalance();

        if(fromBalance >= cents) {

            fromAccount.setBalance(fromAccount.getBalance() - cents);
            toAccount.setBalance(toAccount.getBalance() + cents);
        }

        return status;
    }*/
}
