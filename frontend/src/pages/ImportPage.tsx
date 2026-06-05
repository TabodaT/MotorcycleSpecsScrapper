import { useRef, useState } from 'react';
import { useImportCsv } from '../api/hooks';
import { ErrorMessage } from '../components/ErrorMessage';
import { Card } from '../components/Card';
import type { ImportResultDto } from '../api/types';

export function ImportPage() {
  const fileRef = useRef<HTMLInputElement>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [result, setResult] = useState<ImportResultDto | null>(null);
  const importCsv = useImportCsv();

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    setSelectedFile(e.target.files?.[0] ?? null);
    setResult(null);
  }

  async function handleUpload() {
    if (!selectedFile) return;
    setResult(null);
    try {
      const res = await importCsv.mutateAsync(selectedFile);
      setResult(res);
    } catch {
      // error displayed via importCsv.error
    }
  }

  return (
    <>
      <div className="page-header">
        <div className="page-title">CSV Import</div>
        <div className="page-subtitle">Bulk-import market listings via CSV file</div>
      </div>
      <div className="page-body">
        <div style={{ maxWidth: 700 }}>
          <section className="section">
            <h2 className="section-title">Step 1 – Download Template</h2>
            <Card>
              <div className="card-body">
                <p style={{ marginBottom: 12, color: '#6b7280', fontSize: '0.875rem' }}>
                  Download the CSV template to see the required column headers before preparing your file.
                </p>
                <a
                  href="/api/market/import/template"
                  download="moto_import_template.csv"
                  className="btn btn-secondary"
                >
                  ⬇ Download Template
                </a>
              </div>
            </Card>
          </section>

          <section className="section">
            <h2 className="section-title">Step 2 – Upload CSV</h2>
            <Card>
              <div className="card-body">
                <div
                  className="upload-area"
                  onClick={() => fileRef.current?.click()}
                >
                  {selectedFile ? (
                    <>
                      <div style={{ fontSize: '1.5rem' }}>📄</div>
                      <strong>{selectedFile.name}</strong>
                      <div className="text-sm text-muted">{(selectedFile.size / 1024).toFixed(1)} KB — click to change</div>
                    </>
                  ) : (
                    <>
                      <div style={{ fontSize: '1.5rem' }}>📂</div>
                      <div>Click to select a CSV file</div>
                      <div className="text-sm text-muted">or drag and drop</div>
                    </>
                  )}
                </div>
                <input
                  ref={fileRef}
                  type="file"
                  accept=".csv,text/csv"
                  style={{ display: 'none' }}
                  onChange={handleFileChange}
                />
                <div style={{ marginTop: 16 }}>
                  <button
                    className="btn btn-primary"
                    disabled={!selectedFile || importCsv.isPending}
                    onClick={handleUpload}
                  >
                    {importCsv.isPending ? 'Uploading…' : 'Upload & Import'}
                  </button>
                </div>

                {importCsv.error && (
                  <div style={{ marginTop: 14 }}>
                    <ErrorMessage error={importCsv.error} title="Import Failed" />
                  </div>
                )}
              </div>
            </Card>
          </section>

          {result && (
            <section className="section">
              <h2 className="section-title">Import Result</h2>
              <Card>
                <div className="card-body">
                  <div className="stat-cards-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)', marginBottom: 16 }}>
                    <div className="stat-card">
                      <div className="stat-value" style={{ color: '#059669' }}>{result.inserted}</div>
                      <div className="stat-label">Inserted</div>
                    </div>
                    <div className="stat-card">
                      <div className="stat-value" style={{ color: '#2563eb' }}>{result.updated}</div>
                      <div className="stat-label">Updated</div>
                    </div>
                    <div className="stat-card">
                      <div className="stat-value" style={{ color: result.failed > 0 ? '#dc2626' : undefined }}>{result.failed}</div>
                      <div className="stat-label">Failed</div>
                    </div>
                  </div>
                  <p className="text-sm text-muted">Job ID: <span className="mono">{result.jobId}</span></p>

                  {result.rowErrors && result.rowErrors.length > 0 && (
                    <>
                      <h3 style={{ fontSize: '0.9rem', fontWeight: 600, margin: '16px 0 8px' }}>
                        Row Errors ({result.rowErrors.length})
                      </h3>
                      <div className="table-wrapper">
                        <table className="data-table">
                          <thead>
                            <tr>
                              <th>Row</th>
                              <th>Field</th>
                              <th>Message</th>
                            </tr>
                          </thead>
                          <tbody>
                            {result.rowErrors.map((e, i) => (
                              <tr key={i}>
                                <td>{e.row}</td>
                                <td className="mono">{e.field}</td>
                                <td>{e.message}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    </>
                  )}
                </div>
              </Card>
            </section>
          )}
        </div>
      </div>
    </>
  );
}
