/*
 * Copyright 2012-2015 CodeLibs Project and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.codelibs.fess.es.log.bsbhv;

import java.util.List;
import java.util.Map;

import org.codelibs.fess.es.log.allcommon.EsAbstractBehavior;
import org.codelibs.fess.es.log.allcommon.EsAbstractEntity;
import org.codelibs.fess.es.log.allcommon.EsAbstractEntity.RequestOptionCall;
import org.codelibs.fess.es.log.bsentity.dbmeta.FavoriteLogDbm;
import org.codelibs.fess.es.log.cbean.FavoriteLogCB;
import org.codelibs.fess.es.log.exentity.FavoriteLog;
import org.dbflute.Entity;
import org.dbflute.bhv.readable.CBCall;
import org.dbflute.bhv.readable.EntityRowHandler;
import org.dbflute.cbean.ConditionBean;
import org.dbflute.cbean.result.ListResultBean;
import org.dbflute.cbean.result.PagingResultBean;
import org.dbflute.exception.IllegalBehaviorStateException;
import org.dbflute.optional.OptionalEntity;
import org.dbflute.util.DfTypeUtil;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;

/**
 * @author ESFlute (using FreeGen)
 */
public abstract class BsFavoriteLogBhv extends EsAbstractBehavior<FavoriteLog, FavoriteLogCB> {

    // ===================================================================================
    //                                                                    Control Override
    //                                                                    ================
    @Override
    public String asTableDbName() {
        return asEsIndexType();
    }

    @Override
    protected String asEsIndex() {
        return "fess_log";
    }

    @Override
    public String asEsIndexType() {
        return "favorite_log";
    }

    @Override
    public String asEsSearchType() {
        return "favorite_log";
    }

    @Override
    public FavoriteLogDbm asDBMeta() {
        return FavoriteLogDbm.getInstance();
    }

    @Override
    protected <RESULT extends FavoriteLog> RESULT createEntity(Map<String, Object> source, Class<? extends RESULT> entityType) {
        try {
            final RESULT result = entityType.newInstance();
            result.setCreatedAt(DfTypeUtil.toLocalDateTime(source.get("createdAt")));
            result.setUrl(DfTypeUtil.toString(source.get("url")));
            result.setDocId(DfTypeUtil.toString(source.get("docId")));
            result.setQueryId(DfTypeUtil.toString(source.get("queryId")));
            result.setUserInfoId(DfTypeUtil.toString(source.get("userInfoId")));
            return result;
        } catch (InstantiationException | IllegalAccessException e) {
            final String msg = "Cannot create a new instance: " + entityType.getName();
            throw new IllegalBehaviorStateException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    public int selectCount(CBCall<FavoriteLogCB> cbLambda) {
        return facadeSelectCount(createCB(cbLambda));
    }

    public OptionalEntity<FavoriteLog> selectEntity(CBCall<FavoriteLogCB> cbLambda) {
        return facadeSelectEntity(createCB(cbLambda));
    }

    protected OptionalEntity<FavoriteLog> facadeSelectEntity(FavoriteLogCB cb) {
        return doSelectOptionalEntity(cb, typeOfSelectedEntity());
    }

    protected <ENTITY extends FavoriteLog> OptionalEntity<ENTITY> doSelectOptionalEntity(FavoriteLogCB cb,
            Class<? extends ENTITY> tp) {
        return createOptionalEntity(doSelectEntity(cb, tp), cb);
    }

    @Override
    public FavoriteLogCB newConditionBean() {
        return new FavoriteLogCB();
    }

    @Override
    protected Entity doReadEntity(ConditionBean cb) {
        return facadeSelectEntity(downcast(cb)).orElse(null);
    }

    public FavoriteLog selectEntityWithDeletedCheck(CBCall<FavoriteLogCB> cbLambda) {
        return facadeSelectEntityWithDeletedCheck(createCB(cbLambda));
    }

    public OptionalEntity<FavoriteLog> selectByPK(String id) {
        return facadeSelectByPK(id);
    }

    protected OptionalEntity<FavoriteLog> facadeSelectByPK(String id) {
        return doSelectOptionalByPK(id, typeOfSelectedEntity());
    }

    protected <ENTITY extends FavoriteLog> ENTITY doSelectByPK(String id, Class<? extends ENTITY> tp) {
        return doSelectEntity(xprepareCBAsPK(id), tp);
    }

    protected FavoriteLogCB xprepareCBAsPK(String id) {
        assertObjectNotNull("id", id);
        return newConditionBean().acceptPK(id);
    }

    protected <ENTITY extends FavoriteLog> OptionalEntity<ENTITY> doSelectOptionalByPK(String id, Class<? extends ENTITY> tp) {
        return createOptionalEntity(doSelectByPK(id, tp), id);
    }

    @Override
    protected Class<? extends FavoriteLog> typeOfSelectedEntity() {
        return FavoriteLog.class;
    }

    @Override
    protected Class<FavoriteLog> typeOfHandlingEntity() {
        return FavoriteLog.class;
    }

    @Override
    protected Class<FavoriteLogCB> typeOfHandlingConditionBean() {
        return FavoriteLogCB.class;
    }

    public ListResultBean<FavoriteLog> selectList(CBCall<FavoriteLogCB> cbLambda) {
        return facadeSelectList(createCB(cbLambda));
    }

    public PagingResultBean<FavoriteLog> selectPage(CBCall<FavoriteLogCB> cbLambda) {
        // #pending same?
        return (PagingResultBean<FavoriteLog>) facadeSelectList(createCB(cbLambda));
    }

    public void selectCursor(CBCall<FavoriteLogCB> cbLambda, EntityRowHandler<FavoriteLog> entityLambda) {
        facadeSelectCursor(createCB(cbLambda), entityLambda);
    }

    public void selectBulk(CBCall<FavoriteLogCB> cbLambda, EntityRowHandler<List<FavoriteLog>> entityLambda) {
        delegateSelectBulk(createCB(cbLambda), entityLambda,typeOfSelectedEntity());
    }

    // ===================================================================================
    //                                                                              Update
    //                                                                              ======
    public void insert(FavoriteLog entity) {
        doInsert(entity, null);
    }

    public void insert(FavoriteLog entity, RequestOptionCall<IndexRequestBuilder> opLambda) {
        if (entity instanceof EsAbstractEntity) {
            entity.asDocMeta().indexOption(opLambda);
        }
        doInsert(entity, null);
    }

    public void update(FavoriteLog entity) {
        doUpdate(entity, null);
    }

    public void update(FavoriteLog entity, RequestOptionCall<IndexRequestBuilder> opLambda) {
        if (entity instanceof EsAbstractEntity) {
            entity.asDocMeta().indexOption(opLambda);
        }
        doUpdate(entity, null);
    }

    public void insertOrUpdate(FavoriteLog entity) {
        doInsertOrUpdate(entity, null, null);
    }

    public void insertOrUpdate(FavoriteLog entity, RequestOptionCall<IndexRequestBuilder> opLambda) {
        if (entity instanceof EsAbstractEntity) {
            entity.asDocMeta().indexOption(opLambda);
        }
        doInsertOrUpdate(entity, null, null);
    }

    public void delete(FavoriteLog entity) {
        doDelete(entity, null);
    }

    public void delete(FavoriteLog entity, RequestOptionCall<DeleteRequestBuilder> opLambda) {
        if (entity instanceof EsAbstractEntity) {
            entity.asDocMeta().deleteOption(opLambda);
        }
        doDelete(entity, null);
    }

    public int queryDelete(CBCall<FavoriteLogCB> cbLambda) {
        return doQueryDelete(createCB(cbLambda), null);
    }

    public int[] batchInsert(List<FavoriteLog> list) {
        return batchInsert(list, null);
    }

    public int[] batchInsert(List<FavoriteLog> list, RequestOptionCall<BulkRequestBuilder> call) {
        return doBatchInsert(new BulkList<>(list, call), null);
    }

    public int[] batchUpdate(List<FavoriteLog> list) {
        return batchUpdate(list, null);
    }

    public int[] batchUpdate(List<FavoriteLog> list, RequestOptionCall<BulkRequestBuilder> call) {
        return doBatchUpdate(new BulkList<>(list, call), null);
    }

    public int[] batchDelete(List<FavoriteLog> list) {
        return batchDelete(list, null);
    }

    public int[] batchDelete(List<FavoriteLog> list, RequestOptionCall<BulkRequestBuilder> call) {
        return doBatchDelete(new BulkList<>(list, call), null);
    }

    // #pending create, modify, remove
}

