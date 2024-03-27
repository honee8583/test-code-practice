package shop.mtcoding.bank.domain.transaction;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;

interface Dao {
    List<Transaction> findTransactionList(@Param("accountId") Long accountId, @Param("gubun") String gubun, @Param("page") Integer page);   // 요청값이 여러개일경우 @Param을 붙여줘야 한다.
}

// Impl을 붙여줘야 하고, TransactionRepository가 앞에 붙어야 한다.
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements Dao {
    private final EntityManager em;

    @Override
    public List<Transaction> findTransactionList(Long accountId, String gubun, Integer page) {
        // JPQL
        // 동적쿼리 (gubun이 DEPOSIT일경우 입금내역, WITHDRAW일경우 출금내역, ALL일경우 입출금내역)
        String sql = "";
        sql += "select t from Transaction t ";

        if (gubun.equals("WITHDRAW")) {
            sql += "join fetch t.withdrawAccount wa ";  // 페치조인
            sql += "where t.withdrawAccount.id = :withdrawAccountId";
        } else if (gubun.equals("DEPOSIT")){
            sql += "join fetch t.depositAccount da ";
            sql += "where t.depositAccount.id = :depositAccountId";
        } else {
            sql += "left join fetch t.withdrawAccount wa ";
            sql += "left join fetch t.depositAccount da ";
            sql += "where t.withdrawAccount.id = :withdrawAccountId ";
            sql += "or ";
            sql += "t.depositAccount.id = :depositAccountId";
        }

        TypedQuery<Transaction> query = em.createQuery(sql, Transaction.class);

        if (gubun.equals("WITHDRAW")) {
            query = query.setParameter("withdrawAccountId", accountId);
        } else if (gubun.equals("DEPOSIT")) {
            query = query.setParameter("depositAccountId", accountId);
        } else {
            query = query.setParameter("withdrawAccountId", accountId);
            query = query.setParameter("depositAccountId", accountId);
        }

        query.setFirstResult(page * 5);
        query.setMaxResults(5);

        return query.getResultList();
    }
}
