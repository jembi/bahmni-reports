package org.bahmni.reports.template;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import org.bahmni.reports.BahmniReportsProperties;
import org.bahmni.reports.dao.GenericDao;
import org.bahmni.reports.dao.impl.GenericObservationDaoImpl;
import org.bahmni.reports.model.GenericObservationReportConfig;
import org.bahmni.reports.model.Report;
import org.bahmni.reports.model.UsingDatasource;
import org.bahmni.reports.util.CommonComponents;
import org.bahmni.reports.util.GenericObservationReportTemplateHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.bahmni.reports.util.GenericObservationReportTemplateHelper.*;

@UsingDatasource("openmrs")
public class GenericObservationReportTemplate extends BaseReportTemplate<GenericObservationReportConfig> {

    private BahmniReportsProperties bahmniReportsProperties;

    public GenericObservationReportTemplate(BahmniReportsProperties bahmniReportsProperties) {
        this.bahmniReportsProperties = bahmniReportsProperties;
    }

    @Override
    public JasperReportBuilder build(Connection connection, JasperReportBuilder jasperReport,
                                     Report<GenericObservationReportConfig> report,
                                     String startDate, String endDate, List<AutoCloseable> resources,
                                     PageType pageType) throws SQLException {
        CommonComponents.addTo(jasperReport, report, pageType);


        jasperReport.setShowColumnTitle(true)
                .addPageHeader()
                .setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL);

        List<String> conceptNamesToFilter = new ArrayList<>();

        GenericObservationReportTemplateHelper.createAndAddMandatoryColumns(jasperReport, report.getConfig());
        if (report.getConfig() != null) {
            createAndAddPatientAttributeColumns(jasperReport, report.getConfig());
            createAndAddVisitAttributeColumns(jasperReport, report.getConfig());
            createAndAddPatientAddressColumns(jasperReport, report.getConfig());
            createAndAddProviderNameColumn(jasperReport, report.getConfig());
            createAndAddVisitInfoColumns(jasperReport, report.getConfig());
            if (report.getConfig().isEncounterPerRow()) {
                conceptNamesToFilter = fetchLeafConceptsAsList(report, bahmniReportsProperties);
                createAndAddConceptColumns(conceptNamesToFilter, jasperReport);
            }
            createAndAddDataAnalysisColumns(jasperReport, report.getConfig());
        }
        GenericDao genericObservationDao = new GenericObservationDaoImpl(report, bahmniReportsProperties);

        ResultSet obsResultSet = genericObservationDao.getResultSet(connection, startDate, endDate, conceptNamesToFilter);

        return obsResultSet != null ? jasperReport.setDataSource(obsResultSet) : jasperReport;
    }

}
